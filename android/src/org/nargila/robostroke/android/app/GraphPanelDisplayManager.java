/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.android.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.android.app.roll.RollViewGroup;
import org.nargila.robostroke.android.common.PreviewFrameLayout;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.android.AccellGraphView;
import org.nargila.robostroke.ui.graph.android.StrokeAnalysisGraphView;
import org.nargila.robostroke.ui.graph.android.StrokeGraphView;
import org.nargila.robostroke.ui.graph.android.StrokePowerGraphView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GraphPanelDisplayManager {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphPanelDisplayManager.class);

	/**
	 * layout version - to be increment whenever we want to force layout reset on client device upon app upgrade
	 */
	private static final int LAYOUT_VERSION = 2;

	private final static int DEFAULT_SLOT_COUNT = 1;	
	
	private static final String PENDING_SLOT_RESET_KEY = "pendingSlotReset" + LAYOUT_VERSION;

	private static class OrderInfo<T> {
		int pos;
		T item;
		
		OrderInfo(int pos, T item) {
			super();
			this.pos = pos;
			this.item = item;
		}
		
		static <I> OrderInfo<I> create(int pos, I item) {
			return new OrderInfo<I>(pos, item);
		}
	}
	
	
	private List<OrderInfo<FrameLayout>> slotList;
	private final HashMap<FrameLayout, OrderInfo<FrameLayout>> slotMapping = new HashMap<FrameLayout, OrderInfo<FrameLayout>>();
	private List<OrderInfo<View>> viewList;
	private final HashMap<View, OrderInfo<View>> viewMapping = new HashMap<View, OrderInfo<View>>();
	private final LinkedList<OrderInfo<View>> undisplayedViews = new LinkedList<OrderInfo<View>>();
	
	private final SharedPreferences prefs;

	private int slotCyclicCounter = DEFAULT_SLOT_COUNT - 2;
	
	private final LinearLayout slotContainer;
	private final RoboStrokeActivity owner;

	private AccellGraphView accel_graph;
	private HeartRateView heart_rate_view;
	private StrokePowerGraphView stroke_power_graph;
	private StrokeAnalysisGraphView stroke_analysis_graph;
	private StrokeGraphView stroke_graph;
	private StrokePowerBarGraphView stroke_power_bar_graph;
	private RollViewGroup roll_view_group;
	
	private final long graphXRange = TimeUnit.SECONDS.toNanos(8);

	class PendingReset implements Runnable {
		int delay = 250;
		private ScheduledFuture<?> pending;
		boolean[] restoreStates;
		
		synchronized void trigger() {
			if (pending == null) {
				logger.debug("initializing pending graph reset");
				restoreStates = resetGraphs(true, null);
			} else {
				logger.debug("deffering pending graph reset");
				pending.cancel(true);
			}
			
			pending = owner.scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);				
		}
		@Override
		public synchronized void run() {
			resetGraphs(true, restoreStates);
			pending = null;
		}
	}
	
	private final PendingReset pendingReset = new PendingReset();

	boolean tiltFreezeOn;
	

	public GraphPanelDisplayManager(RoboStrokeActivity owner) {
		
		this.slotContainer = (LinearLayout) owner.findViewById(R.id.graph_container);
		
		this.accel_graph = new AccellGraphView(owner, graphXRange, owner.roboStroke);
		this.heart_rate_view = new HeartRateView(owner, owner.roboStroke);
		this.stroke_power_graph = new StrokePowerGraphView(owner, owner.roboStroke);
		stroke_analysis_graph = new StrokeAnalysisGraphView(owner, owner.roboStroke);
		this.stroke_graph = new StrokeGraphView(owner, graphXRange, owner.roboStroke);
		this.stroke_power_bar_graph = new StrokePowerBarGraphView(owner, owner.roboStroke);
		this.roll_view_group = new RollViewGroup(owner, owner.roboStroke);

		prefs = owner.getSharedPreferences("layout", 0);
		
		this.owner = owner;

	}
	
	private void setVisibleSlotCount(int slots, View stickyView) {
		int i = 0;
		
		int n = (stickyView == null) ? slots : slots - 1;
		
		for (OrderInfo<FrameLayout> slotInfo: slotList) {
			boolean isStickyView = (stickyView != null)
					&& (slotInfo.item.getChildAt(0) == stickyView);
			if (!isStickyView) {
				toggleSlot(slotInfo, (i++ < n), null);
			}
		}
		
		updateSlotContainerWeight(slots);
	}

	private void updateSlotContainerWeight(int slotCount) {
		
		LinearLayout.LayoutParams params = new LayoutParams(slotContainer.getLayoutParams());
		
		switch (slotCount) {
		case 0:
			params.weight = 0f;
			break;			
		case 3:
			params.weight = .4f;
			break;
		case 2:
			params.weight = .6f;
			break;
			default:
				params.weight = 2.5f;
		}
		
		slotContainer.setLayoutParams(params);
	}

	public void toggleSlotView(FrameLayout slot) {
		
		OrderInfo<FrameLayout> slotInfo = slotMapping.get(slot);
		
		if (slotInfo == null) {
			throw new AssertionError("HDIGH!");
		}
		
		toggleSlotView(slotInfo, null);
	}
	
	private void toggleSlotView(OrderInfo<FrameLayout> slotInfo, OrderInfo<View> initView) {
		
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT);

		View oldView = slotInfo.item.getChildAt(0);
		
		OrderInfo<View> newView;
		
		if (initView == null) {
			newView = undisplayedViews.poll();
		} else {
			if (!undisplayedViews.remove(initView)) {
				throw new AssertionError("HDIGH: initView should initially be found in undisplayedViews");
			}
			
			newView = initView;
		}
		
		slotInfo.item.removeAllViews();
		
		slotInfo.item.addView(newView.item, layoutParams);

		if (oldView != null) {
			OrderInfo<View> viewInfo = getViewInfo(oldView);
			
			undisplayedViews.add(viewInfo);
		}
		
		prefs.edit().putInt("slot." + slotInfo.pos, newView.pos).commit();
	}


	private void toggleSlot(OrderInfo<FrameLayout> slotInfo, boolean show, OrderInfo<View> initView) {
		if (show) {
			
			toggleSlotView(slotInfo, initView);
			slotInfo.item.setVisibility(View.VISIBLE);
		
		} else if (slotInfo.item.getVisibility() == View.VISIBLE) {
			
			View recycleView = slotInfo.item.getChildAt(0);

			if (recycleView != null) {
				OrderInfo<View> viewInfo = getViewInfo(recycleView);
				
				undisplayedViews.add(viewInfo);
			}

			slotInfo.item.removeAllViews();
			slotInfo.item.setVisibility(View.GONE);
			
			prefs.edit().remove("slot." + slotInfo.pos).commit();
		}
	}

	private OrderInfo<View> getViewInfo(View view) {
		OrderInfo<View> res = viewMapping.get(view);
		
		if (res == null) {
			throw new AssertionError("HDIGH: view is not in mapping");
		}
		
		return res;
	}
	
	void resetGraphs() {
		resetGraphs(false, null);
	}
	
	private synchronized boolean[] resetGraphs(boolean halfFlush, boolean[] restoreStates) {
		final DataUpdatable[] arr = {
				accel_graph, stroke_graph, stroke_power_graph, stroke_analysis_graph
		};
		
		final boolean[] states = new boolean[arr.length];
		
		int i = 0;
		for (DataUpdatable graph: arr) {
			if (halfFlush) {
				if (restoreStates == null) {

					states[i++] = graph.isDisabled();
					graph.disableUpdate(true);
				} else {
					graph.reset();
					graph.disableUpdate(restoreStates[i++]);
				}
			} else {				
				graph.reset();
			}
		}

		return states;
	}

	void init() {
		
		FrameLayout[] slots = {
				(FrameLayout) owner.findViewById(R.id.graph_frame1),
				(FrameLayout) owner.findViewById(R.id.graph_frame2),
				(FrameLayout) owner.findViewById(R.id.graph_frame3) 
			};
			
		View[] viewArr = {
				new PreviewFrameLayout(owner, R.drawable.graph_accel_400, accel_graph),
				new PreviewFrameLayout(owner, R.drawable.graph_power_bar_400, stroke_power_bar_graph), 
				new PreviewFrameLayout(owner, R.drawable.graph_analysis_400, stroke_analysis_graph),
				new PreviewFrameLayout(owner, R.drawable.graph_power_400, stroke_power_graph), 
				new PreviewFrameLayout(owner, R.drawable.graph_stroke_400, stroke_graph), 
				roll_view_group, 
				heart_rate_view
		};
		
		LinkedList<View> views = new LinkedList<View>();
		
		for (View view: viewArr) { // add only non black-listed views
			if (view.getTag() == null || !view.getTag().equals("blackList")) {
				views.add(view);
			}
		}		
		
		slotList = new ArrayList<OrderInfo<FrameLayout>>(slots.length);
		viewList = new ArrayList<OrderInfo<View>>(views.size());

		{
			int i = 0;
			for (FrameLayout frame: slots) {
				OrderInfo<FrameLayout> orderInfo = OrderInfo.create(i++, frame);
				slotList.add(orderInfo);
				slotMapping.put(frame, orderInfo);
				frame.setVisibility(View.GONE);
			}
		}	
		
		{
			int i = 0;
			for (View view: views) {
				OrderInfo<View> orderInfo = OrderInfo.create(i++, view);
				viewList.add(orderInfo);
				viewMapping.put(view, orderInfo);
			}
		}
		
		undisplayedViews.addAll(viewList);

		if (prefs.getBoolean(PENDING_SLOT_RESET_KEY, true)) {
			
			SharedPreferences.Editor prefEdit = prefs.edit();
			
			prefEdit.clear();
			prefEdit.putBoolean(PENDING_SLOT_RESET_KEY, false);
			prefEdit.commit();		
		}
		

		slotCyclicCounter = prefs.getInt("slot.count", slotCyclicCounter);
		
		
		
		for (final View view: views) {

			final GestureDetector gd = new GestureDetector(
					new GestureDetector.SimpleOnGestureListener() {

						@Override
						public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
							float vx = Math.abs(velocityX);
							float vy = Math.abs(velocityY);
							if (vx > vy) { // left/right fling: forward/rewind
								// replay
								if (owner.isReplaying()) {
									pendingReset.trigger(); // post graph data flush/reset request
									owner.roboStroke.getDataInput().skipReplayTime(
											velocityX);
								}
							} else if (velocityY > 0) { // fling down
								if (owner.isReplaying())
									owner.togglePause();
							} else { // fling up
								toggleSlotView((FrameLayout) view.getParent());
							}

							return true;
						}

						@Override
						public boolean onDoubleTap(MotionEvent event) {
							toggleSlotCount(view);

							return true;
						};

						@Override
						public boolean onSingleTapConfirmed(MotionEvent event) {
							if (view == roll_view_group) {
								roll_view_group.setMode(null);
								return true;
							}

							return super.onSingleTapConfirmed(event);
						}

						@Override
						public void onLongPress(MotionEvent event) {

							if (view == roll_view_group) {
								if (tiltFreezeOn) {
									owner.showDialog(R.layout.tilt_freeze_dialog);
								} else {
									if (RoboStrokeActivity.m_AlertDlg != null) {
										RoboStrokeActivity.m_AlertDlg.cancel();
									}


									RoboStrokeActivity.m_AlertDlg = new AlertDialog.Builder(owner)
									.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											owner.showDialog(R.layout.tilt_freeze_dialog);
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})

									.setMessage(owner.getString(R.string.tilt_freeze_warning).replace("${CALIBRATION_SECONDS}", RoboStrokeConstants.TILT_FREEZE_CALIBRATION_TIME+""))
									.setTitle("Tilt Freeze")
									.setIcon(R.drawable.icon)
									.setCancelable(true)
									.show();						
								}
							}
						}
					});

			view.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gd.onTouchEvent(event);
					return true;
				}
			});
		}
		
		boolean firstRun = prefs.getBoolean("firstRun", true);

		if (firstRun) {

			nextSlotCount(null);
			prefs.edit().putBoolean("firstRun", false).commit();

		} else {

			slotCyclicCounter = -1;

			for (int i = 0; i < slotList.size(); ++i) {
				int viewId = prefs.getInt("slot." + i, -1);

				if (viewId != -1) {
					OrderInfo<FrameLayout> slotInfo = slotList.get(i);
					OrderInfo<View> viewInfo = viewList.get(viewId);

					toggleSlot(slotInfo, true, viewInfo);
					slotCyclicCounter++;
				}
			}

			int slotCount = slotCyclicCounter + 1;

			updateSlotContainerWeight(slotCount);
			
			owner.metersDisplayManager.onUpdateGraphSlotCount(slotCount);
		}
	}

	public void setShowGraphs(boolean showGraphs) {
		slotContainer.setVisibility(showGraphs ? View.VISIBLE : View.GONE);
	}
	
	void setEnableHrm(boolean enable, boolean resetNextRun) {
		
		if (heart_rate_view != null) {
			this.heart_rate_view.setTag(enable ? null : "blackList");

			if (resetNextRun) {
				resetNextRun();
			}
		}
	}

	public void toggleSlotCount(View view) {
		
		if (view == null) {
			throw new IllegalArgumentException();
		}
		
		nextSlotCount(view);
		
	}
	
	private void nextSlotCount(View view) {

		int nextCount = (++slotCyclicCounter % slotList.size()) + 1;
		
		setVisibleSlotCount(nextCount, view);	
		
		owner.metersDisplayManager.onUpdateGraphSlotCount(nextCount);
	}

	public void resetNextRun() {
		prefs.edit().putBoolean(PENDING_SLOT_RESET_KEY, true).commit();		
	}
}

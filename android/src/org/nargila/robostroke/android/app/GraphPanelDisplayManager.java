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

import android.content.SharedPreferences;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class GraphPanelDisplayManager {
	
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
	
	
	private final List<OrderInfo<FrameLayout>> slotList;
	private final HashMap<FrameLayout, OrderInfo<FrameLayout>> slotMapping = new HashMap<FrameLayout, OrderInfo<FrameLayout>>();
	private final List<OrderInfo<View>> viewList;
	private final HashMap<View, OrderInfo<View>> viewMapping = new HashMap<View, OrderInfo<View>>();
	private final LinkedList<OrderInfo<View>> undisplayedViews = new LinkedList<OrderInfo<View>>();
	
	private final SharedPreferences prefs;

	private int slotCyclicCounter = DEFAULT_SLOT_COUNT - 2;
	
	private final LinearLayout slotContainer;
	private final RoboStrokeActivity owner;


	public GraphPanelDisplayManager(RoboStrokeActivity owner, LinearLayout slotContainer, FrameLayout[] slots, View[] views) {
		slotList = new ArrayList<OrderInfo<FrameLayout>>(slots.length);
		viewList = new ArrayList<OrderInfo<View>>(views.length);
		this.slotContainer = slotContainer;
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

		prefs = owner.getSharedPreferences("layout", 0);
		
		if (prefs.getBoolean(PENDING_SLOT_RESET_KEY, true)) {
			
			SharedPreferences.Editor prefEdit = prefs.edit();
			
			prefEdit.clear();
			prefEdit.putBoolean(PENDING_SLOT_RESET_KEY, false);
			prefEdit.commit();		
		}
		
		slotCyclicCounter = prefs.getInt("slot.count", slotCyclicCounter);
		
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
		params.weight = (slotCount == 3 ? .4f : 
							(slotCount == 2 ? .6f : 2.5f));
								
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
	
	public void restore() {
		
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
			owner.onUpdateGraphSlotCount(slotCount);
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
		
		owner.onUpdateGraphSlotCount(nextCount);
	}

	public void resetNextRun() {
		prefs.edit().putBoolean(PENDING_SLOT_RESET_KEY, true).commit();		
	}
}

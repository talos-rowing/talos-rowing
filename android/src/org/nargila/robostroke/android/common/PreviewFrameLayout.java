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
package org.nargila.robostroke.android.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.nargila.robostroke.common.SimpleLock;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PreviewFrameLayout extends FrameLayout {

	private final Handler mainHanlder = new Handler();
	
	private final View preview;
	private final int previewTime;
	boolean previewOn;
	
	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactory() {

		private final SimpleLock lock = new SimpleLock();
		private int counter;

		@Override
		public Thread newThread(Runnable r) {
			synchronized (lock) {
				Thread th = new Thread(r, "RoboStrokeActivity scheduler thread " + (++counter));
				th.setDaemon(true);
				return th;
			}						
		}
	});
	
	
	Runnable removePreview = new Runnable() {
		
		@Override
		public void run() {
			try {
				Thread.sleep(previewTime);
			} catch (InterruptedException e) {
			}
			
			mainHanlder.post(new Runnable() {
				
				@Override
				public void run() {
					showPreview(false);
				}
			});
		}
	};
	
	private final View content;
	
	public PreviewFrameLayout(Context context, final int drawable, View content) {
		super(context);
		
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		ImageView img = new ImageView(context);
		img.setImageResource(drawable);
		this.preview = img;
		this.previewTime = 2000;
		this.content = content;		
	}
	
	@Override
	protected void onAttachedToWindow() {
		showPreview(true);
		super.onAttachedToWindow();
	}
	
	private synchronized void showPreview(boolean show) {
				
		if (show) {
			if (previewOn) { // already previewing
				return;
			}
			removeAllViews();
			addView(preview);
			postInvalidate();
			EXECUTOR.execute(removePreview);
		} else {
			removeView(preview);
			addView(content, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			postInvalidate();
		}		
		
		previewOn = show;
	}
}

package org.nargila.robostroke.ui;

public interface RSView extends HasVisibility, HasBackgroundColor {
    void setOnLongClickListener(RSLongClickListener listener);

    void setOnClickListener(RSClickListener listener);

    void setOnDoubleClickListener(RSDoubleClickListener listener);
}

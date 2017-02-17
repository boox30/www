package com.onyx.kreader.ui.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.onyx.kreader.R;
import com.onyx.kreader.ui.view.ZoomImageView;

/**
 * Created by joy on 2/13/17.
 */

public class DialogImageView extends DialogBase {

    public DialogImageView(Context context, Bitmap bitmap) {
        super(context, android.R.style.Theme_NoTitleBar_Fullscreen);

        setContentView(R.layout.dialog_image_view);

        findViewById(R.id.image_view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        final ZoomImageView imageView = ((ZoomImageView) findViewById(R.id.image_view));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogImageView.this.cancel();
            }
        });
        imageView.setImageBitmap(bitmap);
    }

}

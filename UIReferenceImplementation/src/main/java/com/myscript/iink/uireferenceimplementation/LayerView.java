// Copyright MyScript. All rights reserved.

package com.myscript.iink.uireferenceimplementation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.myscript.iink.IRenderTarget;
import com.myscript.iink.IRenderTarget.LayerType;
import com.myscript.iink.Renderer;

import java.util.Map;

public class LayerView extends View
{
  private LayerType type;
  private IRenderTarget renderTarget;

  private ImageLoader imageLoader;

  @Nullable
  private Map<String, Typeface> typefaceMap;

  @Nullable
  private Renderer lastRenderer = null;

  @Nullable
  private Rect updateArea;
  @Nullable
  private Bitmap bitmap;
  @Nullable
  private android.graphics.Canvas sysCanvas;
  @Nullable
  private Canvas iinkCanvas;

  public LayerView(Context context)
  {
    this(context, null, 0);
  }

  public LayerView(Context context, @Nullable AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public LayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);

    updateArea = null;

    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LayerView, defStyleAttr, 0);
    try
    {
      int typeOrdinal = typedArray.getInteger(R.styleable.LayerView_layerType, 0);
      type = LayerType.values()[typeOrdinal];
    }
    finally
    {
      typedArray.recycle();
    }
  }

  public void setImageLoader(ImageLoader imageLoader)
  {
    this.imageLoader = imageLoader;
  }

  public void setCustomTypefaces(Map<String, Typeface> typefaceMap)
  {
    this.typefaceMap = typefaceMap;
  }

  public void setRenderTarget(IRenderTarget renderTarget)
  {
    this.renderTarget = renderTarget;
  }

  public LayerType getType()
  {
    return type;
  }

  @Override
  protected final void onDraw(android.graphics.Canvas canvas)
  {
    Rect updateArea;
    Renderer renderer;

    synchronized (this)
    {
      updateArea = this.updateArea;
      this.updateArea = null;
      renderer = lastRenderer;
      lastRenderer = null;
    }

    if (updateArea != null)
    {

      prepare(sysCanvas, updateArea);
      try
      {
        switch (type)
        {
          case BACKGROUND:
            renderer.drawBackground(updateArea.left, updateArea.top, updateArea.width(), updateArea.height(), iinkCanvas);
            break;
          case MODEL:
            renderer.drawModel(updateArea.left, updateArea.top, updateArea.width(), updateArea.height(), iinkCanvas);
            break;
          case TEMPORARY:
            renderer.drawTemporaryItems(updateArea.left, updateArea.top, updateArea.width(), updateArea.height(), iinkCanvas);
            break;
          case CAPTURE:
            renderer.drawCaptureStrokes(updateArea.left, updateArea.top, updateArea.width(), updateArea.height(), iinkCanvas);
            break;
          default:
            // unknown layer type
            break;
        }
      }
      finally
      {
        restore(sysCanvas);
      }
    }

    canvas.drawBitmap(bitmap, 0, 0, null);
  }

  @Override
  protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight)
  {
    if (bitmap != null)
    {
      bitmap.recycle();
    }
    bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
    sysCanvas = new android.graphics.Canvas(bitmap);
    iinkCanvas = new Canvas(sysCanvas, typefaceMap, imageLoader, renderTarget);

    super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
  }

  private void prepare(android.graphics.Canvas canvas, Rect clipRect)
  {
    canvas.save();
    canvas.clipRect(clipRect);
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
  }

  private void restore(android.graphics.Canvas canvas)
  {
    canvas.restore();
  }

  public final void update(Renderer renderer, int x, int y, int width, int height)
  {
    synchronized (this)
    {
      if (updateArea != null)
        updateArea.union(x, y, x + width, y + height);
      else
        updateArea = new Rect(x, y, x + width, y + height);

      lastRenderer = renderer;
    }

    postInvalidate(x, y, x + width, y + height);
  }
}

package com.github.rodrigohenriques.samples.customview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import java.util.List;

@CoordinatorLayout.DefaultBehavior(CoordinatedImageView.Behavior.class)
public class CoordinatedImageView extends ImageView {

    private boolean mIsHiding;
    private OnChangeVisibilityListener onChangeVisibilityListener;

    public CoordinatedImageView(Context context) {
        super(context);
    }

    public CoordinatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoordinatedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CoordinatedImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void hide() {
        if(!this.mIsHiding && this.getVisibility() == VISIBLE) {
            if(ViewCompat.isLaidOut(this) && !this.isInEditMode()) {
                this.animate().scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setDuration(200L).setInterpolator(new FastOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        CoordinatedImageView.this.mIsHiding = true;
                        CoordinatedImageView.this.setVisibility(VISIBLE);
                    }

                    public void onAnimationCancel(Animator animation) {
                        CoordinatedImageView.this.mIsHiding = false;
                    }

                    public void onAnimationEnd(Animator animation) {
                        CoordinatedImageView.this.mIsHiding = false;
                        CoordinatedImageView.this.setVisibility(GONE);
                    }
                });
            } else {
                this.setVisibility(GONE);
            }
        }
    }

    public void show() {
        if(this.getVisibility() != VISIBLE) {
            if(ViewCompat.isLaidOut(this) && !this.isInEditMode()) {
                this.setAlpha(0.0F);
                this.setScaleY(0.0F);
                this.setScaleX(0.0F);
                this.animate().scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setDuration(200L).setInterpolator(new FastOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        CoordinatedImageView.this.setVisibility(VISIBLE);
                    }
                });
            } else {
                this.setVisibility(VISIBLE);
                this.setAlpha(1.0F);
                this.setScaleY(1.0F);
                this.setScaleX(1.0F);
            }
        }
    }

    public void setOnChangeVisibilityListener(OnChangeVisibilityListener onChangeVisibilityListener) {
        this.onChangeVisibilityListener = onChangeVisibilityListener;
    }

    public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<CoordinatedImageView> {
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
        private Rect mTmpRect;

        public Behavior() {
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, CoordinatedImageView child, View dependency) {
            return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, CoordinatedImageView child, View dependency) {
            if(dependency instanceof Snackbar.SnackbarLayout) {
                this.updateFabTranslationForSnackbar(parent, child, dependency);
            } else if(dependency instanceof AppBarLayout) {
                this.updateFabVisibility(parent, (AppBarLayout)dependency, child);
            }

            return false;
        }

        public void onDependentViewRemoved(CoordinatorLayout parent, CoordinatedImageView child, View dependency) {
            if(dependency instanceof Snackbar.SnackbarLayout && ViewCompat.getTranslationY(child) != 0.0F) {
                ViewCompat.animate(child).translationY(0.0F).scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setInterpolator(new FastOutSlowInInterpolator()).setListener((ViewPropertyAnimatorListener)null);
            }

        }

        private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, CoordinatedImageView child) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)child.getLayoutParams();
            if(lp.getAnchorId() != appBarLayout.getId()) {
                return false;
            } else {
                if(this.mTmpRect == null) {
                    this.mTmpRect = new Rect();
                }

                Rect rect = this.mTmpRect;
                ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);
                if(rect.bottom <= getMinimumHeightForVisibleOverlappingContent(appBarLayout)) {
                    child.hide();

                    if (child.onChangeVisibilityListener != null)
                        child.onChangeVisibilityListener.onHide();
                } else {
                    child.show();

                    if (child.onChangeVisibilityListener != null)
                        child.onChangeVisibilityListener.onShow();
                }

                return true;
            }
        }

        private int getMinimumHeightForVisibleOverlappingContent(AppBarLayout appBarLayout) {
            int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
            if(minHeight != 0) {
                return minHeight * 2;
            } else {
                int childCount = appBarLayout.getChildCount();
                return childCount >= 1?ViewCompat.getMinimumHeight(appBarLayout.getChildAt(childCount - 1)) * 2 :0;
            }
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, CoordinatedImageView imageView, View snackbar) {
            if(imageView.getVisibility() == 0) {
                float translationY = this.getFabTranslationYForSnackbar(parent, imageView);
                ViewCompat.setTranslationY(imageView, translationY);
            }
        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent, CoordinatedImageView imageView) {
            float minOffset = 0.0F;
            List dependencies = parent.getDependencies(imageView);
            int i = 0;

            for(int z = dependencies.size(); i < z; ++i) {
                View view = (View)dependencies.get(i);
                if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(imageView, view)) {
                    minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
                }
            }

            return minOffset;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, CoordinatedImageView child, int layoutDirection) {
            List dependencies = parent.getDependencies(child);
            int i = 0;

            for(int count = dependencies.size(); i < count; ++i) {
                View dependency = (View)dependencies.get(i);
                if(dependency instanceof AppBarLayout && this.updateFabVisibility(parent, (AppBarLayout)dependency, child)) {
                    break;
                }
            }

            parent.onLayoutChild(child, layoutDirection);
            this.offsetIfNeeded(parent, child);
            return true;
        }

        private void offsetIfNeeded(CoordinatorLayout parent, CoordinatedImageView imageView) {
            Rect padding = new Rect(imageView.getPaddingLeft(), imageView.getPaddingTop(), imageView.getPaddingRight(), imageView.getPaddingBottom());
            if(padding.centerX() > 0 && padding.centerY() > 0) {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)imageView.getLayoutParams();
                int offsetTB = 0;
                int offsetLR = 0;
                if(imageView.getRight() >= parent.getWidth() - lp.rightMargin) {
                    offsetLR = padding.right;
                } else if(imageView.getLeft() <= lp.leftMargin) {
                    offsetLR = -padding.left;
                }

                if(imageView.getBottom() >= parent.getBottom() - lp.bottomMargin) {
                    offsetTB = padding.bottom;
                } else if(imageView.getTop() <= lp.topMargin) {
                    offsetTB = -padding.top;
                }

                imageView.offsetTopAndBottom(offsetTB);
                imageView.offsetLeftAndRight(offsetLR);
            }

        }

        static {
            SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        }
    }

    private static class ViewGroupUtils {
        private static final ViewGroupUtils.ViewGroupUtilsImpl IMPL;

        ViewGroupUtils() {
        }

        static void offsetDescendantRect(ViewGroup parent, View descendant, Rect rect) {
            IMPL.offsetDescendantRect(parent, descendant, rect);
        }

        static void getDescendantRect(ViewGroup parent, View descendant, Rect out) {
            out.set(0, 0, descendant.getWidth(), descendant.getHeight());
            offsetDescendantRect(parent, descendant, out);
        }

        static {
            int version = Build.VERSION.SDK_INT;
            if(version >= 11) {
                IMPL = new ViewGroupUtils.ViewGroupUtilsImplHoneycomb();
            } else {
                IMPL = new ViewGroupUtils.ViewGroupUtilsImplBase();
            }

        }

        private static class ViewGroupUtilsImplHoneycomb implements ViewGroupUtils.ViewGroupUtilsImpl {
            private ViewGroupUtilsImplHoneycomb() {
            }

            public void offsetDescendantRect(ViewGroup parent, View child, Rect rect) {
                ViewGroupUtilsHoneycomb.offsetDescendantRect(parent, child, rect);
            }
        }

        private static class ViewGroupUtilsImplBase implements ViewGroupUtils.ViewGroupUtilsImpl {
            private ViewGroupUtilsImplBase() {
            }

            public void offsetDescendantRect(ViewGroup parent, View child, Rect rect) {
                parent.offsetDescendantRectToMyCoords(child, rect);
            }
        }

        private interface ViewGroupUtilsImpl {
            void offsetDescendantRect(ViewGroup var1, View var2, Rect var3);
        }
    }
    
    private static class ViewGroupUtilsHoneycomb {
        private static final ThreadLocal<Matrix> sMatrix = new ThreadLocal();
        private static final ThreadLocal<RectF> sRectF = new ThreadLocal();
        private static final Matrix IDENTITY = new Matrix();

        ViewGroupUtilsHoneycomb() {
        }

        public static void offsetDescendantRect(ViewGroup group, View child, Rect rect) {
            Matrix m = (Matrix)sMatrix.get();
            if(m == null) {
                m = new Matrix();
                sMatrix.set(m);
            } else {
                m.set(IDENTITY);
            }

            offsetDescendantMatrix(group, child, m);
            RectF rectF = (RectF)sRectF.get();
            if(rectF == null) {
                rectF = new RectF();
            }

            rectF.set(rect);
            m.mapRect(rectF);
            rect.set((int)(rectF.left + 0.5F), (int)(rectF.top + 0.5F), (int)(rectF.right + 0.5F), (int)(rectF.bottom + 0.5F));
        }

        static void offsetDescendantMatrix(ViewParent target, View view, Matrix m) {
            ViewParent parent = view.getParent();
            if(parent instanceof View && parent != target) {
                View vp = (View)parent;
                offsetDescendantMatrix(target, vp, m);
                m.preTranslate((float)(-vp.getScrollX()), (float)(-vp.getScrollY()));
            }

            m.preTranslate((float)view.getLeft(), (float)view.getTop());
            if(!view.getMatrix().isIdentity()) {
                m.preConcat(view.getMatrix());
            }

        }
    }

    public interface OnChangeVisibilityListener {
        void onHide();
        void onShow();
    }
}

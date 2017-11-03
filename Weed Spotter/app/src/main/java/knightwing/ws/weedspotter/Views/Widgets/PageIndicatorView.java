package knightwing.ws.weedspotter.Views.Widgets;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Matthew Christensen on 6/09/17.
 * Displays an indication of where the user is in their submission.
 */

public class PageIndicatorView extends android.support.v7.widget.AppCompatTextView {

    // 'Dots' displayed on screen - filled is where the user is
    protected final char FILLED_DOT = '●';
    protected final char HOLLOW_DOT = '○';

    // Number of pages, the page where user is currently.
    protected int totalSteps;
    protected int currentStep;

    public PageIndicatorView(Context context) {
        super(context);
        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTotalSteps(3);
        setCurrentStep(1);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTotalSteps(3);
        setCurrentStep(1);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTotalSteps(3);
        setCurrentStep(1);
    }

    /**
     * Set the length of the page indicator.
     * @param n - the number of steps in the indicator.
     * @require n >= 1
     * @ensure dynamic changing with the number of fragments.
     */
    public void setTotalSteps(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Need at least 1 step.");
        }
        this.totalSteps = n;
        if (currentStep > n) {
            currentStep = totalSteps;
        }
    }

    /**
     * Set where the user is currently at along the indicator.
     * @param n - where the user is now at.
     * @require n > 0 && n < totalSteps
     * @ensure it can be clearly seen where the user is up to.
     */
    public void setCurrentStep(int n) {
        if (n >= totalSteps || n < 0) {
            throw new IllegalArgumentException("Cannot set step that doesn't exit.");
        }
        currentStep = n;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalSteps; i++) {
            if (i == currentStep) {
                sb.append(FILLED_DOT);
            } else {
                sb.append(HOLLOW_DOT);
            }
        }
        setText(sb.toString());
    }
}

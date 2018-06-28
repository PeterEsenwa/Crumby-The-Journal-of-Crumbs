package ng.petersabs.dev.crumby;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class DaysRecyclerView extends RecyclerView {
    public DaysRecyclerView(Context context) {
        super(context);
    }

    public DaysRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DaysRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int smoothScrollToPositionWithReturn(int position) {
        super.smoothScrollToPosition(position);
        return ( (LinearLayoutManager) this.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }
}

package face.camera.beans.base.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import face.camera.beans.R;
import face.camera.beans.base.adapter.CommonAdapter;
import face.camera.beans.base.beans.BaseRecyclerBean;
import face.camera.beans.base.utils.APermissionUtils;
import face.camera.beans.base.viewHolder.IBaseRecyclerItemClickListener;

public abstract class RVBaseActivity extends AppCompatActivity implements IBaseRecyclerItemClickListener {
    public final String TAG = this.getClass().getCanonicalName();
    @BindView(R.id.base_rv)
    public RecyclerView mBaseRv;
    public CommonAdapter mCommonAdapter;
    public LinearLayoutManager mLinearLayoutManager;

    public List<BaseRecyclerBean> mBaseRecyclerBeen = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);
        initGetData();
        initView();
    }

    protected abstract void initGetData();


    protected void initView() {
        mCommonAdapter = new CommonAdapter(this, this, mBaseRecyclerBeen,this);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBaseRv.setLayoutManager(mLinearLayoutManager);
        mBaseRv.setAdapter(mCommonAdapter);
        APermissionUtils.checkPermission(this);
    }
}

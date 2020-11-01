package org.smartregister.brac.hnpp.activity;

import org.smartregister.brac.hnpp.fragment.HnppRiskElcoMemberRegisterFragment;
import org.smartregister.view.fragment.BaseRegisterFragment;


public class HnppElcoRiskRegisterActivity extends HnppElcoMemberRegisterActivity {



    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppRiskElcoMemberRegisterFragment();
    }
}

package com.pinaki.jobdispatcher.jobdispatcherwthrxjava.recipe;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.google.android.gms.common.util.ArrayUtils;
import com.pinaki.jobdispatcher.jobdispatcherwthrxjava.R;
import com.pinaki.jobdispatcher.jobdispatcherwthrxjava.core.JobScheduerFacade;
import com.pinaki.jobdispatcher.jobdispatcherwthrxjava.databinding.ActivityMainBinding;
import com.pinaki.jobdispatcher.jobdispatcherwthrxjava.recipe.jobs.SimpleJobService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.rgJobType.setOnCheckedChangeListener((group, checkedId) -> {
            // show recurring frequency edit text only when recurring radio button is selected
            binding.llRecurringFrequency.setVisibility((checkedId == R.id.rbRecurring) ? View.VISIBLE : View.GONE);
        });

        // listen to button clicks
        binding.btnStart.setOnClickListener(this);
        binding.btnStop.setOnClickListener(this);

        // listen to check changes
        binding.cbConstraintAnyNetwork.setOnCheckedChangeListener(this);
        binding.cbConstraintUnmeteredNetwork.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Either unmetered or any network constraint can be applied.
        // If one is checked, uncheck the other one
        if(buttonView.isChecked()){
            if (buttonView.equals(binding.cbConstraintAnyNetwork)) {
                if (binding.cbConstraintUnmeteredNetwork.isChecked()) {
                    binding.cbConstraintUnmeteredNetwork.setChecked(false);
                }
            } else if (buttonView.equals(binding.cbConstraintUnmeteredNetwork)) {
                if (binding.cbConstraintAnyNetwork.isChecked()) {
                    binding.cbConstraintAnyNetwork.setChecked(false);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnStart) {
            startJobService();
        } else if (id == R.id.btnStop) {
            cancelExistingJob();
        }
    }

    private void startJobService() {
        boolean isOneOffTask = true;

        int checkedRadioButtonId = binding.rgJobType.getCheckedRadioButtonId();

        int recurringFreq = 0;
        if (checkedRadioButtonId == R.id.rbRecurring) {
            isOneOffTask = false;

            // validation for recurring jobs
            String recurringFrequencyString = binding.etRecurringFrequency.getText().toString();
            if (recurringFrequencyString.isEmpty()) {
                showToast(getString(R.string.msg_err_valid_recurring_frequency));
                return;
            }

            try {
                recurringFreq = Integer.parseInt(recurringFrequencyString);

                if (recurringFreq == 0) {
                    showToast(getString(R.string.msg_err_valid_recurring_frequency));
                    return;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // common code for recurring and one off jobs
        boolean shouldUpdateCurrent = binding.cbUpdateCurrent.isChecked();

        // trigger the job
        if (isOneOffTask) {
            JobScheduerFacade.scheduleOneOffJob(this, SimpleJobService.class, TagProvider.ONE_OFF_SIMPLE_SERVICE, null, shouldUpdateCurrent, getConstraints());
        } else {
            JobScheduerFacade.schedulePeriodicJob(this, SimpleJobService.class, TagProvider.ONE_OFF_SIMPLE_SERVICE, null, shouldUpdateCurrent, recurringFreq, getConstraints());
        }
    }

    @NotNull
    private int[] getConstraints() {
        List<Integer> constraints = new ArrayList<>();

        if (binding.cbConstraintDeviceCharging.isChecked())
            constraints.add(Constraint.DEVICE_CHARGING);

        if (binding.cbConstraintDeviceIdle.isChecked())
            constraints.add(Constraint.DEVICE_IDLE);

        if (binding.cbConstraintAnyNetwork.isChecked())
            constraints.add(Constraint.ON_ANY_NETWORK);

        if (binding.cbConstraintUnmeteredNetwork.isChecked())
            constraints.add(Constraint.ON_UNMETERED_NETWORK);

        if (constraints.isEmpty())
            return new int[]{}; // return an empty array

        Integer[] addedConstraints = constraints.toArray(new Integer[constraints.size()]);

        return ArrayUtils.toPrimitiveArray(addedConstraints);
    }

    private void cancelExistingJob() {
        boolean isOneOffTask = true;

        int checkedRadioButtonId = binding.rgJobType.getCheckedRadioButtonId();

        if (checkedRadioButtonId == R.id.rbRecurring) {
            isOneOffTask = false;
        }
        boolean success;
        if (isOneOffTask) {
            success = JobScheduerFacade.cancelJob(this, TagProvider.ONE_OFF_SIMPLE_SERVICE);
        } else {
            success = JobScheduerFacade.cancelJob(this, TagProvider.RECURRING_SIMPLE_SERVICE);
        }

        showToast(success ? getString(R.string.msg_job_cancelled) : getString(R.string.msg_job_couldnt_be_cancelled));
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

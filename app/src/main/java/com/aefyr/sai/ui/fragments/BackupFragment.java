package com.aefyr.sai.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aefyr.sai.R;
import com.aefyr.sai.adapters.BackupPackagesAdapter;
import com.aefyr.sai.model.common.PackageMeta;
import com.aefyr.sai.ui.dialogs.BackupAllSplitApksDialogFragment;
import com.aefyr.sai.ui.dialogs.BackupDialogFragment;
import com.aefyr.sai.ui.dialogs.OneTimeWarningDialogFragment;
import com.aefyr.sai.utils.Utils;
import com.aefyr.sai.viewmodels.BackupViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;

public class BackupFragment extends SaiBaseFragment implements BackupPackagesAdapter.OnItemInteractionListener {


    private EditText mEditTextSearch;
    private Chip mChipFilterSplitsOnly;
    private Chip mChipFilterIncludeSystemApps;

    private BackupViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OneTimeWarningDialogFragment.showIfNeeded(requireContext(), getChildFragmentManager(), R.string.help, R.string.backup_warning, "backup_faq");

        mViewModel = ViewModelProviders.of(this).get(BackupViewModel.class);


        RecyclerView recyclerView = findViewById(R.id.rv_packages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 24);

        BackupPackagesAdapter adapter = new BackupPackagesAdapter(getContext());
        adapter.setInteractionListener(this);
        recyclerView.setAdapter(adapter);

        setupToolbar();

        mViewModel.getPackages().observe(this, adapter::setData);
    }

    private void setupToolbar() {
        //Search
        mEditTextSearch = findViewById(R.id.et_search);
        mChipFilterSplitsOnly = findViewById(R.id.chip_filter_splits);
        mChipFilterIncludeSystemApps = findViewById(R.id.chip_filter_system);

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterPackages();
            }
        });

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (group, checkedId) -> filterPackages();
        mChipFilterSplitsOnly.setOnCheckedChangeListener(onCheckedChangeListener);
        mChipFilterIncludeSystemApps.setOnCheckedChangeListener(onCheckedChangeListener);
        filterPackages();

        //Menu
        TextInputLayout textInputLayout = findViewById(R.id.til);
        textInputLayout.setEndIconOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.backup_fragment, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener((menuItem) -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_export_all_split_apks:
                        exportAllSplitApks();
                        break;
                }
                return true;
            });

            popupMenu.show();
        });
    }

    private void filterPackages() {
        mViewModel.filter(mEditTextSearch.getText().toString(), mChipFilterSplitsOnly.isChecked(), mChipFilterIncludeSystemApps.isChecked());
    }

    private void exportAllSplitApks() {
        BackupAllSplitApksDialogFragment.newInstance().show(getChildFragmentManager(), null);
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_backup;
    }

    @Override
    public void onBackupButtonClicked(PackageMeta packageMeta) {
        BackupDialogFragment.newInstance(packageMeta).show(getChildFragmentManager(), null);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden)
            Utils.hideKeyboard(this);
    }
}

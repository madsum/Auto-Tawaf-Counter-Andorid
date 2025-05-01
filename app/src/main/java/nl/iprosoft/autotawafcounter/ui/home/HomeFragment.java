package nl.iprosoft.autotawafcounter.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import nl.iprosoft.autotawafcounter.R;
import nl.iprosoft.autotawafcounter.activity.TawafActivity;
import nl.iprosoft.autotawafcounter.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
         this.homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Observe navigation state changes
        homeViewModel.getNavigationState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case HomeViewModel.STATE_INITIAL:
                    showAlignmentDialog();
                    break;
                case HomeViewModel.STATE_NOT_ALIGNED:
                    showNotAlignedDialog();
                    break;
                case HomeViewModel.STATE_START_TAWAF:
                    startTawafActivity();
                    break;
            }
        });

        return root;
    }

private void showAlignmentDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(R.string.dialog_alignment_title)
            .setMessage(R.string.dialog_alignment_message)
            .setCancelable(false)
            .setPositiveButton(R.string.button_yes_start_tawaf, (dialog, which) -> {
                homeViewModel.onYesStartTawafClicked();
            })
            .setNegativeButton(R.string.button_not_yet, (dialog, which) -> {
                homeViewModel.onNotYetClicked();
            });

    AlertDialog dialog = builder.create();
    dialog.show();
}

    private void showNotAlignedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_not_aligned_title)
                .setMessage(R.string.dialog_not_aligned_message)
                .setCancelable(false)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    homeViewModel.onNotAlignedOkClicked();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void startTawafActivity() {
        Intent intent = new Intent(requireContext(), TawafActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
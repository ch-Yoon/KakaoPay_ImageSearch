package com.ch.yoon.kakao.pay.imagesearch.ui.imagesearch.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ch.yoon.kakao.pay.imagesearch.databinding.ItemRetryFooterBinding;
import com.ch.yoon.kakao.pay.imagesearch.ui.imagesearch.adapter.OnFooterItemClickListener;

/**
 * Creator : ch-yoon
 * Date : 2019-08-04.
 */
public class RetryFooterViewHolder extends RecyclerView.ViewHolder {

    private final ItemRetryFooterBinding binding;

    public RetryFooterViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

    public void setRetryVisibility(boolean visible) {
        binding.setRetryButtonVisibility(visible);
    }

    public void setOnFooterItemClickListener(@Nullable OnFooterItemClickListener onFooterItemClickListener) {
        binding.retryButton.setOnClickListener(v -> {
            if(onFooterItemClickListener != null) {
                onFooterItemClickListener.onClick();
            }
        });
    }

}

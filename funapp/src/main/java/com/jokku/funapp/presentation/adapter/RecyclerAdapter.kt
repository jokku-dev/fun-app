package com.jokku.funapp.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jokku.funapp.R
import com.jokku.funapp.presentation.*

class RecyclerAdapter<T>(
    private val listener: FavoriteItemClickListener<T>,
    private val communicator: Communicator<T>
) : RecyclerView.Adapter<RecyclerAdapter.FunViewHolder<T>>() {

    fun update() {
        val result = communicator.getDiffResult()
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunViewHolder<T> {
        val emptyList = viewType == 0
        val view = LayoutInflater.from(parent.context).inflate(
            if (emptyList) R.layout.empty_list_item else R.layout.fun_list_item,
            parent, false
        )
        return if (emptyList) FunViewHolder.Empty(view) else FunViewHolder.Base(view, listener)
    }

    override fun onBindViewHolder(holder: FunViewHolder<T>, position: Int) {
        holder.bind(communicator.getList()[position])
    }

    override fun getItemCount(): Int {
        return communicator.getList().size
    }

    override fun getItemViewType(position: Int) = when (communicator.getList()[position]) {
        is FailedUiModel -> 0
        else -> 1
    }

    abstract class FunViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
        private val textView = itemView.findViewById<CorrectTextView>(R.id.common_tv)
        open fun bind(model: UiModel<T>) {
            model.setText(textView)
        }

        class Base<T>(view: View, private val listener: FavoriteItemClickListener<T>) :
            FunViewHolder<T>(view) {
            private val imageView = itemView.findViewById<CorrectImageButton>(R.id.list_favorite_ib)
            override fun bind(model: UiModel<T>) {
                super.bind(model)
                imageView.setOnClickListener {
                    model.changeStatus(listener)
                }
            }
        }

        class Empty<T>(view: View) : FunViewHolder<T>(view)
    }

    interface FavoriteItemClickListener<T> {
        fun changeStatus(id: T)
    }
}
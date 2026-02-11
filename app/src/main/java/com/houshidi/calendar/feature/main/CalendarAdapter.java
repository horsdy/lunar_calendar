package com.houshidi.calendar.feature.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.houshidi.calendar.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<CalendarDayModel> dayList = new ArrayList<>();
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(CalendarDayModel day);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set days and sync selected state from ViewModel (F-003: no selection when switching month).
     */
    public void setDays(List<CalendarDayModel> days, CalendarDayModel selectedDay) {
        this.dayList = days;
        selectedPosition = -1;
        if (selectedDay != null) {
            for (int i = 0; i < days.size(); i++) {
                if (days.get(i).timeMillis == selectedDay.timeMillis) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDayModel model = dayList.get(position);
        holder.tvDay.setText(model.day);
        holder.tvLunarDay.setText(model.lunarDay);

        // Highlight selected day
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_day);
        } else if (model.isToday) {
            holder.itemView.setBackgroundResource(R.drawable.bg_today_circle);
        } else {
            holder.itemView.setBackground(null);
        }

        // Text color logic
        if (model.isToday || position == selectedPosition) {
            holder.tvDay.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.calendar_today_text));
        } else {
            holder.tvDay.setTextColor(model.isCurrentMonth ? 
                ContextCompat.getColor(holder.itemView.getContext(), R.color.calendar_text_primary) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.calendar_text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onItemClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        TextView tvLunarDay;

        ViewHolder(View view) {
            super(view);
            tvDay = view.findViewById(R.id.tv_day);
            tvLunarDay = view.findViewById(R.id.tv_lunar_day);
        }
    }
}

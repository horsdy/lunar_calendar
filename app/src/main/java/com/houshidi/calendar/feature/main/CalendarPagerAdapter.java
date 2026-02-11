package com.houshidi.calendar.feature.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.houshidi.calendar.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.ViewHolder> {

    private final CalendarViewModel viewModel;
    private final boolean startFromMonday;
    private static final int MAX_PAGES = 2400; // 200 years

    public CalendarPagerAdapter(CalendarViewModel viewModel, boolean startFromMonday) {
        this.viewModel = viewModel;
        this.startFromMonday = startFromMonday;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, position - MAX_PAGES / 2);

        CalendarAdapter adapter = new CalendarAdapter();
        adapter.setOnItemClickListener(day -> viewModel.selectDay(day));

        holder.rvCalendar.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 7));
        holder.rvCalendar.setAdapter(adapter);

        List<CalendarDayModel> days = viewModel.calculateDaysForMonth(cal, startFromMonday);
        adapter.setDays(days, viewModel.getSelectedDay().getValue());
    }

    @Override
    public int getItemCount() {
        return MAX_PAGES;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvCalendar;

        ViewHolder(View view) {
            super(view);
            rvCalendar = view.findViewById(R.id.rv_calendar_grid);
        }
    }
}

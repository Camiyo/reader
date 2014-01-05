package com.sismics.reader.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;

import com.androidquery.AQuery;
import com.sismics.reader.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Adapter for categories list.
 * 
 * @author bgamard
 */
public class CategoryAdapter extends BaseAdapter {

    /**
     * Items in list.
     */
    private List<Category> items;

    /**
     * Modified states.
     */
    private Queue<State> states;

    /**
     * Context.
     */
    private Context context;

    /**
     * AQuery.
     */
    private AQuery aq;

    /**
     * Constructor.
     * @param context Context
     * @param items Categories
     */
    public CategoryAdapter(Context context, List<Category> items) {
        this.context = context;
        this.items = items;
        this.states = new LinkedList<State>();
        this.aq = new AQuery(context);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Category item = getItem(position);
        
        // Inflating the right layout
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout = R.layout.manage_list_item_category;
            view = vi.inflate(layout, null);
        }
        
        // Recycling AQuery
        aq.recycle(view);

        // Open the overflow menu
        aq.id(R.id.overflow).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the overflow menu
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.category_overflow_menu);

                // Attach actions listeners
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.rename:
                                return true;

                            case R.id.delete:
                                delete(position);
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                // Show the overflow menu
                popupMenu.show();
            }
        });

        aq.id(R.id.title).text(item.getTitle());
        
        return view;
    }

    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Category getItem(int position) {
        try {
            return items.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Move an item.
     * @param from From position
     * @param to To this position
     */
    public void move(int from, int to) {
        if (getItem(from) == null) {
            return;
        }
        Category category = items.remove(from);
        items.add(to, category);
        State state = new State(category.getId(), State.Type.MOVE, to);
        states.add(state);
        notifyDataSetChanged();
    }

    /**
     * Delete an item.
     * @param position Item to delete
     */
    public void delete(final int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.category_delete_title)
                .setMessage(R.string.category_delete_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Category category = getItem(position);
                        if (category == null) {
                            return;
                        }
                        items.remove(position);
                        State state = new State(category.getId(), State.Type.DELETE, position);
                        states.add(state);
                        notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Returns modified states.
     * @return states
     */
    public Queue<State> getStates() {
        return states;
    }

    /**
     * Clear states.
     */
    public void clearStates() {
        states.clear();
    }

    /**
     * A category.
     */
    public static class Category {

        private String id;
        private String title;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    /**
     * A modified state.
     */
    public static class State {

        public static enum Type {
            DELETE, MOVE
        }

        private String id;
        private Type type;
        private int position;

        public State(String id, Type type, int position) {
            this.id = id;
            this.type = type;
            this.position = position;
        }

        public String getId() {
            return id;
        }

        public Type getType() {
            return type;
        }

        public int getPosition() {
            return position;
        }
    }
}

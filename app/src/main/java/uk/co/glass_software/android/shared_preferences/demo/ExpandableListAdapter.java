package uk.co.glass_software.android.shared_preferences.demo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

class ExpandableListAdapter extends BaseExpandableListAdapter {
    
    private final LinkedList<String> headers;
    private final LinkedHashMap<String, List<String>> children;
    private final LayoutInflater inflater;
    private final MainPresenter presenter;
    
    ExpandableListAdapter(Context context,
                          MainPresenter presenter) {
        this.presenter = presenter;
        headers = new LinkedList<>();
        children = new LinkedHashMap<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    void showEntries() {
        headers.clear();
        children.clear();
        
        addEntries("App opened",
                   "Count: " + presenter.counter().get(1) + " time(s)",
                   "Last open date: " + presenter.lastOpenDate().get("N/A")
        );
        
        addEntries("Plain text entries",
                   presenter.store().getCachedValues()
        );
        
        addEntries("Encrypted entries (as returned by the store)",
                   presenter.encryptedStore().getCachedValues()
        );
        
        addEntries("Encrypted entries (as stored on disk)",
                   presenter.encryptedPreferences().getAll()
        );
        
        notifyDataSetChanged();
    }
    
    private void addEntries(String header,
                            Map<String, ?> entries) {
        Observable.fromIterable(entries.entrySet())
                  .map(entry -> entry.getKey() + " => " + entry.getValue())
                  .toList()
                  .map(list -> list.toArray(new String[list.size()]))
                  .subscribe(list -> addEntries(header, list));
    }
    
    private void addEntries(String header,
                            String... subSections) {
        List<String> info = new ArrayList<>();
        headers.add(header);
        
        Observable.just(subSections)
                  .map(Arrays::asList)
                  .flatMap(Observable::fromIterable)
                  .map(string -> string.replaceAll("\\n", ""))
                  .toList()
                  .subscribe(list -> info.addAll(list));
        
        children.put(header, info);
    }
    
    @Override
    public Object getChild(int groupPosition,
                           int childPosition) {
        String key = headers.get(groupPosition);
        return children.get(key).get(childPosition);
    }
    
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    @Override
    public View getGroupView(int groupPosition,
                             boolean isExpanded,
                             View convertView,
                             ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_group, null);
        }
        
        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        
        return convertView;
    }
    
    @Override
    public View getChildView(int groupPosition,
                             final int childPosition,
                             boolean isLastChild,
                             View convertView,
                             ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        
        TextView txtListChild = convertView.findViewById(R.id.lblListItem);
        
        txtListChild.setText(childText);
        return convertView;
    }
    
    @Override
    public int getChildrenCount(int groupPosition) {
        return children.get(headers.get(groupPosition)).size();
    }
    
    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }
    
    @Override
    public int getGroupCount() {
        return headers.size();
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition,
                                     int childPosition) {
        return false;
    }
}
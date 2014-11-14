package it.skarafaz.mercury.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.skarafaz.mercury.R;
import it.skarafaz.mercury.data.Command;
import it.skarafaz.mercury.listener.OnCommandDetailsListener;
import it.skarafaz.mercury.listener.OnCommandExecListener;

public class CommandListAdapter extends ArrayAdapter<Command> {
    private Context context;

    public CommandListAdapter(Context context, List<Command> commands) {
        super(context, R.layout.command_list_item, commands);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Command command = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.command_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.layout = (LinearLayout) convertView.findViewById(R.id.container);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.cmd = (TextView) convertView.findViewById(R.id.cmd);
            viewHolder.label = (LinearLayout) convertView.findViewById(R.id.label);
            viewHolder.play = (ImageView) convertView.findViewById(R.id.play);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 == 0) {
            viewHolder.layout.setBackgroundColor(context.getResources().getColor(R.color.list_even));
        } else {
            viewHolder.layout.setBackgroundColor(context.getResources().getColor(R.color.list_odd));
        }
        viewHolder.name.setText(command.getName());
        viewHolder.cmd.setText(command.getCmd());
        viewHolder.label.setOnClickListener(new OnCommandDetailsListener(command));
        viewHolder.play.setOnClickListener(new OnCommandExecListener(command));
        return convertView;
    }

    static class ViewHolder {
        LinearLayout layout;
        TextView name;
        TextView cmd;
        LinearLayout label;
        ImageView play;
    }
}

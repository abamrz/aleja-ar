package com.example.AlejaGuidanceSystem.Utility;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.AlejaGuidanceSystem.Graph.Node;


/*
Object binding Node and layoutView, the object being displayed in AR, for all node types
Still missing Waypoints
 */

public class LabelView {

    private Node node;
    private ObjectInReference objectInReference;
    private LinearLayout layoutView;
    private boolean visible;

    public LabelView(Context context, Node node) {
        this.node = node;

        layoutView = new LinearLayout(context);
        layoutView.setOrientation(LinearLayout.VERTICAL);
        layoutView.setVisibility(View.GONE);

        visible = false;

        if (node.getType() == Node.NodeType.OFFICE) {
            final TextView title = new TextView(context);
            // title.setText(Node.typeStrings.get(node.getType()) + (node.getLabel() != null ? (": " + node.getLabel()) : ""));
            title.setText(node.getType().toStringInContext(context) + (node.getLabel() != null ? (": " + node.getLabel()) : ""));
            title.setInputType(InputType.TYPE_CLASS_TEXT);
            title.setTextColor(android.graphics.Color.WHITE);
            title.setBackgroundColor(android.graphics.Color.argb(160, 0, 0, 255));

            final TextView description = new TextView(context);
            description.setText(node.getDescription());
            description.setInputType(InputType.TYPE_CLASS_TEXT);
            description.setTextColor(android.graphics.Color.WHITE);
            description.setBackgroundColor(android.graphics.Color.argb(160, 0, 0, 255));

            layoutView.addView(title);
            layoutView.addView(description);
        }

        else if(node.getType() == Node.NodeType.WAYPOINT) {
            return;
        }

        else {

            final TextView popUpInfo = new TextView(context);
            String labelText = node.getLabel();
            //popUpInfo.setText(node.typeStrings.get(node.getType()) + (labelText != null && !labelText.equals("") ? (": " + labelText) : ""));
            popUpInfo.setText(node.getType().toStringInContext(context) + (labelText != null && !labelText.equals("") ? (": " + labelText) : ""));
            popUpInfo.setInputType(InputType.TYPE_CLASS_TEXT);
            popUpInfo.setTextColor(android.graphics.Color.WHITE);
            layoutView.addView(popUpInfo);


            if (node.getType() == Node.NodeType.KITCHEN) {
                popUpInfo.setBackgroundColor(android.graphics.Color.argb(160, 9, 109, 81));
            } else if (node.getType() == Node.NodeType.EXIT) {
                popUpInfo.setBackgroundColor(android.graphics.Color.GREEN);
            } else if (node.getType() == Node.NodeType.COFFEE) {
                popUpInfo.setBackgroundColor(android.graphics.Color.argb(160, 151, 91, 59));
            } else if (node.getType() == Node.NodeType.ELEVATOR) {
                popUpInfo.setBackgroundColor(android.graphics.Color.argb(160, 194, 197, 204));
            } else if (node.getType() == Node.NodeType.TOILETTE) {
                popUpInfo.setTextColor(android.graphics.Color.BLACK);
                popUpInfo.setBackgroundColor(android.graphics.Color.WHITE);
            } else if (node.getType() == Node.NodeType.FIRE_EXTINGUISHER) {
                popUpInfo.setBackgroundColor(android.graphics.Color.RED);
            }
        }
    }

    public LinearLayout getLayoutView() {
        return this.layoutView;
    }

    public Node getNode() {
        return this.node;
    }

    public boolean getVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) layoutView.setVisibility(View.VISIBLE);
        else layoutView.setVisibility(View.GONE);
    }

    public ObjectInReference getObjectInReference() {
        return this.objectInReference;
    }

    public void setObjectInReference(final ObjectInReference objectInReference) {
        this.objectInReference = objectInReference;
    }
}

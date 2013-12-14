package com.hookmobile.ageui;

import android.app.Activity;
import android.widget.LinearLayout;

/**
 * Created by Andrey Chorniy on December 13, 2013
 * 
 */
public interface InviterProvider {

    /**
     *
     * @return Name from which invite will be sent
     */
    String getName();

    void setName(String name);

    public static class StaticValueInviterProvider implements InviterProvider {
        private String name;

        public StaticValueInviterProvider(String name) {
            this.name = name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

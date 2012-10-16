package com.hookmobile.ageui;

class CheckListViewItem {
	CharSequence name = null;
	CharSequence phone = null;
	boolean checkState = false;

	public CheckListViewItem(String name, String phone, boolean checkState) {
		this.name = name;
		this.phone = phone;
		this.checkState = checkState;
	}

	public CheckListViewItem(String name, String phone) {
		this.name = name;
		this.phone = phone;
		this.checkState = false;
	}
}

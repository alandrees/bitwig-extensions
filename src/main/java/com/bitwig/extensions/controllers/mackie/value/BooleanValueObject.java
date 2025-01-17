package com.bitwig.extensions.controllers.mackie.value;

import java.util.ArrayList;
import java.util.List;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.controller.api.HardwareAction;
import com.bitwig.extension.controller.api.HardwareActionBindable;
import com.bitwig.extension.controller.api.HardwareActionBinding;
import com.bitwig.extension.controller.api.SettableBooleanValue;

public class BooleanValueObject implements SettableBooleanValue {

	private boolean value = false;
	private final List<BooleanValueChangedCallback> callbacks = new ArrayList<BooleanValueChangedCallback>();

	@Override
	public void markInterested() {
	}

	@Override
	public void toggle() {
		this.value = !this.value;
		for (final BooleanValueChangedCallback booleanValueChangedCallback : callbacks) {
			booleanValueChangedCallback.valueChanged(value);
		}
	}

	@Override
	public void addValueObserver(final BooleanValueChangedCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}
	}

	@Override
	public boolean isSubscribed() {
		return !callbacks.isEmpty();
	}

	@Override
	public void setIsSubscribed(final boolean value) {
	}

	@Override
	public void subscribe() {
	}

	@Override
	public void unsubscribe() {
	}

	@Override
	public void set(final boolean value) {
		this.value = value;
		for (final BooleanValueChangedCallback booleanValueChangedCallback : callbacks) {
			booleanValueChangedCallback.valueChanged(value);
		}
	}

	@Override
	public boolean get() {
		return value;
	}

	@Override
	public HardwareActionBinding addBinding(final HardwareAction action) {
		return null;
	}

	@Override
	public void invoke() {
	}

	@Override
	public HardwareActionBindable toggleAction() {
		return null;
	}

	@Override
	public HardwareActionBindable setToTrueAction() {
		return null;
	}

	@Override
	public HardwareActionBindable setToFalseAction() {
		return null;
	}

}

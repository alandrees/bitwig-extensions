package com.bitwig.extensions.controllers.mackie.configurations;

import java.util.function.BiConsumer;

import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.PinnableCursorDevice;
import com.bitwig.extensions.controllers.mackie.MackieMcuProExtension;
import com.bitwig.extensions.controllers.mackie.VPotMode;
import com.bitwig.extensions.controllers.mackie.bindings.ButtonBinding;
import com.bitwig.extensions.controllers.mackie.devices.CursorDeviceControl;
import com.bitwig.extensions.controllers.mackie.devices.DeviceManager;
import com.bitwig.extensions.controllers.mackie.devices.DeviceTypeFollower;
import com.bitwig.extensions.controllers.mackie.devices.ParameterPage;
import com.bitwig.extensions.controllers.mackie.display.DisplayLayer;
import com.bitwig.extensions.controllers.mackie.display.RingDisplayType;
import com.bitwig.extensions.controllers.mackie.layer.EncoderLayer;
import com.bitwig.extensions.controllers.mackie.layer.MixerLayerGroup;
import com.bitwig.extensions.controllers.mackie.section.InfoSource;
import com.bitwig.extensions.controllers.mackie.section.MixControl;
import com.bitwig.extensions.controllers.mackie.section.MixerSectionHardware;
import com.bitwig.extensions.controllers.mackie.section.ParamElement;
import com.bitwig.extensions.controllers.mackie.value.ModifierValueObject;
import com.bitwig.extensions.framework.Layer;
import com.bitwig.extensions.framework.Layers;

public class TrackLayerConfiguration extends LayerConfiguration {

	protected final Layer faderLayer;
	protected final EncoderLayer encoderLayer;
	protected final DisplayLayer displayLayer;
	protected final DisplayLayer infoLayer;

	protected DeviceManager deviceManager;

	private CursorDeviceControl cursorDeviceControl;
	protected DeviceMenuConfiguration menuConfig;

	public TrackLayerConfiguration(final String name, final MixControl mixControl) {
		super(name, mixControl);
		final Layers layers = this.mixControl.getDriver().getLayers();
		final int sectionIndex = mixControl.getHwControls().getSectionIndex();

		faderLayer = new Layer(layers, name + "_FADER_LAYER_" + sectionIndex);
		encoderLayer = new EncoderLayer(mixControl, name + "_ENCODER_LAYER_" + sectionIndex);
		displayLayer = new DisplayLayer(name, this.mixControl);
		infoLayer = new DisplayLayer(name + "_INFO", this.mixControl);
		infoLayer.enableFullTextMode(true);
	}

	public void setDeviceManager(final DeviceManager deviceManager, final DeviceMenuConfiguration menuConfig) {
		final MackieMcuProExtension driver = mixControl.getDriver();
		cursorDeviceControl = driver.getCursorDeviceControl();
		this.deviceManager = deviceManager;
		this.deviceManager.setInfoLayer(infoLayer);
		this.menuConfig = menuConfig;

		final CursorRemoteControlsPage remotes = cursorDeviceControl.getRemotes();
		final PinnableCursorDevice device = cursorDeviceControl.getCursorDevice();
		if (remotes != null) {
			remotes.pageCount().addValueObserver(
					count -> evaluateTextDisplay(count, deviceManager.isSpecificDevicePresent(), device.name().get()));
		}
		device.name().addValueObserver(name -> {
			evaluateTextDisplay(deviceManager.getPageCount(), deviceManager.isSpecificDevicePresent(), name);
		});
	}

	@Override
	public void doActivate() {
		if (menuConfig != null) {
			menuConfig.setDeviceManager(deviceManager);
		}
	}

	@Override
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	@Override
	public boolean applyModifier(final ModifierValueObject modvalue) {
		if (menuConfig != null) {
			return menuConfig.applyModifier(modvalue);
		}
		return false;
	}

	public void registerFollowers(final DeviceTypeFollower... deviceTypeFollowers) {
		final PinnableCursorDevice cursorDevice = cursorDeviceControl.getCursorDevice();

		cursorDevice.exists().addValueObserver(cursorExists -> {
			if (isActive() && !cursorExists && deviceManager.isSpecificDevicePresent()) {
				cursorDevice.selectDevice(deviceManager.getCurrentFollower().getFocusDevice());
				mixControl.getIsMenuHoldActive().set(false);
			}
		});

		for (final DeviceTypeFollower deviceTypeFollower : deviceTypeFollowers) {
			final Device focusDevice = deviceTypeFollower.getFocusDevice();

			focusDevice.exists().addValueObserver(exist -> {
				if (deviceManager.getCurrentFollower() == deviceTypeFollower && isActive()) {
					evaluateTextDisplay(deviceManager.getPageCount(), exist, cursorDevice.name().get());
				}
			});
		}
	}

	@Override
	public void setCurrentFollower(final DeviceTypeFollower follower) {
		if (deviceManager == null) {
			return;
		}
		deviceManager.setCurrentFollower(follower);
		evaluateTextDisplay(deviceManager.getPageCount(), deviceManager.isSpecificDevicePresent(),
				cursorDeviceControl.getCursorDevice().name().get());
	}

	private void evaluateTextDisplay(final int count, final boolean exists, final String deviceName) {
		if (deviceManager == null) {
			return;
		}
		if (menuConfig != null) {
			menuConfig.evaluateTextDisplay(deviceName);
		}
		final CursorRemoteControlsPage remotes = cursorDeviceControl.getRemotes();
		if (remotes != null) {
			if (!exists || deviceName.length() == 0) {
				setMainText(deviceManager.getCurrentFollower().getPotMode());
			} else if (count == 0) {
				displayLayer.setMainText(deviceName + " has no Parameter Pages",
						"<<configure Parameter Pages in Bitwig Studio>>", true);
				displayLayer.enableFullTextMode(true);
			} else {
				displayLayer.enableFullTextMode(false);
			}
		} else if (!exists) {
			setMainText(deviceManager.getCurrentFollower().getPotMode());
		} else {
			displayLayer.enableFullTextMode(false);
		}
	}

	private void setMainText(final VPotMode mode) {
		final String line1 = String.format("no %s on track", mode.getTypeDisplayName());
		final String line2;
		if (mode.getDeviceName() == null) {
			line2 = String.format("<< press %s again to browse >>", mode.getButtonDescription());
		} else {
			line2 = String.format("<< press %s again to create %s device >>", mode.getButtonDescription(),
					mode.getDeviceName());
		}
		displayLayer.setMainText(line1, line2, true);
		displayLayer.enableFullTextMode(true);
	}

	@Override
	public Layer getFaderLayer() {
		final boolean flipped = this.mixControl.isFlipped();
		if (flipped) {
			return faderLayer;
		}
		return this.mixControl.getActiveMixGroup().getFaderLayer(ParamElement.VOLUME);
	}

	private boolean isMenuActive() {
		return menuConfig != null && mixControl.getIsMenuHoldActive().get();
	}

	@Override
	public EncoderLayer getEncoderLayer() {
		if (isMenuActive()) {
			return menuConfig.getEncoderLayer();
		}
		final boolean flipped = this.mixControl.isFlipped();
		final MixerLayerGroup activeMixGroup = this.mixControl.getActiveMixGroup();
		if (flipped) {
			return activeMixGroup.getEncoderLayer(ParamElement.VOLUME);
		}
		return encoderLayer;
	}

	@Override
	public DisplayLayer getDisplayLayer(final int which) {
		if (isMenuActive()) {
			return menuConfig.getDisplayLayer();
		}
		if (deviceManager != null && deviceManager.getInfoSource() != null) {
			return infoLayer;
		}
		if (which == 0) {
			return displayLayer;
		}
		final MixerLayerGroup activeMixGroup = this.mixControl.getActiveMixGroup();
		return activeMixGroup.getDisplayConfiguration(ParamElement.VOLUME);
	}

	@Override
	public boolean enableInfo(final InfoSource type) {
		if (deviceManager != null) {
			deviceManager.enableInfo(type);
		}
		return true;
	}

	@Override
	public boolean disableInfo() {
		if (deviceManager != null) {
			deviceManager.disableInfo();
		}
		return true;
	}

	public void addBinding(final int index, final ParameterPage parameter,
			final BiConsumer<Integer, ParameterPage> resetAction) {
		final MixerSectionHardware hwControls = mixControl.getHwControls();

		encoderLayer.addBinding(parameter.getRelativeEncoderBinding(hwControls.getEncoder(index)));
		encoderLayer.addBinding(parameter.createRingBinding(hwControls.getRingDisplay(index)));
		encoderLayer.addBinding(new ButtonBinding(hwControls.getEncoderPress(index),
				hwControls.createAction(() -> resetAction.accept(index, parameter))));

		faderLayer.addBinding(parameter.getFaderBinding(hwControls.getVolumeFader(index)));
		faderLayer.addBinding(parameter.createFaderBinding(hwControls.getMotorFader(index)));

		displayLayer.bind(index, parameter);
		parameter.resetBindings();
	}

	public void addBinding(final int index, final Parameter parameter, final RingDisplayType type) {
		final MixerSectionHardware hwControls = mixControl.getHwControls();

		faderLayer.addBinding(hwControls.createMotorFaderBinding(index, parameter));
		faderLayer.addBinding(hwControls.createFaderParamBinding(index, parameter));
		faderLayer.addBinding(hwControls.createFaderTouchBinding(index, () -> {
			if (mixControl.getModifier().isShift()) {
				parameter.reset();
			}
		}));
		encoderLayer.addBinding(hwControls.createEncoderPressBinding(index, parameter));
		encoderLayer.addBinding(hwControls.createEncoderToParamBinding(index, parameter));
		encoderLayer.addBinding(hwControls.createRingDisplayBinding(index, parameter, type));
		displayLayer.bindName(index, parameter.name());
		displayLayer.bindParameterValue(index, parameter);
	}

}
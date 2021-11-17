package com.bitwig.extensions.controllers.mackie.configurations;

import com.bitwig.extensions.controllers.mackie.display.DisplayLayer;
import com.bitwig.extensions.controllers.mackie.layer.EncoderLayer;
import com.bitwig.extensions.controllers.mackie.layer.MixerLayerGroup;
import com.bitwig.extensions.controllers.mackie.section.MixControl;
import com.bitwig.extensions.controllers.mackie.section.ParamElement;
import com.bitwig.extensions.framework.Layer;

public class MixerLayerConfiguration extends LayerConfiguration {

	ParamElement encoderAssign;

	public MixerLayerConfiguration(final String name, final MixControl mixControl, final ParamElement encoderAssign) {
		super(name, mixControl);
		this.encoderAssign = encoderAssign;
	}

	@Override
	public Layer getFaderLayer() {
		final boolean flipped = this.mixControl.isFlipped();
		final MixerLayerGroup activeGroup = this.mixControl.getActiveMixGroup();
		if (flipped) {
			return activeGroup.getFaderLayer(encoderAssign);
		} else {
			return activeGroup.getFaderLayer(ParamElement.VOLUME);
		}
	}

	@Override
	public EncoderLayer getEncoderLayer() {
		final boolean flipped = this.mixControl.isFlipped();
		final MixerLayerGroup activeGroup = this.mixControl.getActiveMixGroup();

		if (flipped) {
			return activeGroup.getEncoderLayer(ParamElement.VOLUME);
		} else {
			return activeGroup.getEncoderLayer(encoderAssign);
		}
	}

	@Override
	public Layer getButtonLayer() {
		return this.mixControl.getActiveMixGroup().getMixerButtonLayer();
	}

	@Override
	public DisplayLayer getDisplayLayer(final int which) {
		final MixerLayerGroup activeGroup = this.mixControl.getActiveMixGroup();
		if (which == 0) {
			return activeGroup.getDisplayConfiguration(encoderAssign);
		}
		return activeGroup.getDisplayConfiguration(ParamElement.VOLUME);
	}

}
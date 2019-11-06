package com.bitwig.extensions.framework2;

import java.util.function.BooleanSupplier;

import com.bitwig.extension.controller.api.BoolHardwareOutputValue;

class BooleanSupplierOutputValueBinding extends Binding<BooleanSupplier, BoolHardwareOutputValue>
{
   public BooleanSupplierOutputValueBinding(final BooleanSupplier source, final BoolHardwareOutputValue target)
   {
      super(target, source, target);
   }

   @Override
   protected void deactivate()
   {
      getTarget().setValue(false);
   }

   @Override
   protected void activate()
   {
      getTarget().setValueSupplier(getSource());
   }

}
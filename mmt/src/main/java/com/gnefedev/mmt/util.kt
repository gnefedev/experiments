package com.gnefedev.mmt

import java.beans.Introspector

/**
 * Created by gerakln on 06.11.16.
 */
public fun Class<*>.getBeanName():String = Introspector.decapitalize(this.simpleName)
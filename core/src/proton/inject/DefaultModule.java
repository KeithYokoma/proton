package proton.inject;

import static proton.inject.util.Validator.checkNotNull;
import static proton.inject.util.Validator.checkState;

import java.lang.annotation.Annotation;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import proton.inject.binding.BindingBuilder;
import proton.inject.binding.BindingBuilderImpl;
import proton.inject.binding.Bindings;
import proton.inject.listener.FieldListener;
import proton.inject.listener.FieldListeners;
import proton.inject.listener.ProviderListener;
import proton.inject.listener.ProviderListeners;
import proton.inject.observer.ObserverManager;
import proton.inject.observer.ObserverRegister;
import proton.inject.provider.AccountManagerProvider;
import proton.inject.provider.ApplicationProvider;
import proton.inject.provider.ContextProvider;
import proton.inject.provider.HandlerProvider;
import proton.inject.provider.SystemServiceProvider;
import proton.inject.scope.ApplicationScoped;

public class DefaultModule implements Module {
	private Bindings mBindings;
	private ProviderListeners mProviderListeners;
	private FieldListeners mFieldListeners;

	@SuppressWarnings("rawtypes")
	private static final Class mAccountManagerClass = loadClass("android.accounts.AccountManager");

	@Override
	public final synchronized void configure(Bindings bindings, ProviderListeners providerListeners,
			FieldListeners fieldListeners) {
		checkState(mBindings == null, "Re-entry is not allowed.");

		mBindings = checkNotNull(bindings, "bindings");
		mProviderListeners = checkNotNull(providerListeners, "providerListeners");
		mFieldListeners = checkNotNull(fieldListeners, "fieldListeners");

		try {
			configure();
		} finally {
			mProviderListeners = null;
			mBindings = null;
		}
	}

	private static Class<?> loadClass(String className) {
		try {
			return Class.forName(className);
		} catch (Throwable t) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void configure() {
		bind(Application.class).toProvider(ApplicationProvider.class).in(ApplicationScoped.class);
		bind(Context.class).toProvider(ContextProvider.class);
		bind(Handler.class).toProvider(HandlerProvider.class).in(ApplicationScoped.class);

		bind(ActivityManager.class).toProvider(new SystemServiceProvider<ActivityManager>(Context.ACTIVITY_SERVICE));
		bind(AlarmManager.class).toProvider(new SystemServiceProvider<AlarmManager>(Context.ALARM_SERVICE));
		bind(AudioManager.class).toProvider(new SystemServiceProvider<AudioManager>(Context.AUDIO_SERVICE));
		bind(ConnectivityManager.class).toProvider(
				new SystemServiceProvider<ConnectivityManager>(Context.CONNECTIVITY_SERVICE));
		bind(InputMethodManager.class).toProvider(
				new SystemServiceProvider<InputMethodManager>(Context.INPUT_METHOD_SERVICE));
		bind(KeyguardManager.class).toProvider(new SystemServiceProvider<KeyguardManager>(Context.KEYGUARD_SERVICE));

		bind(LocationManager.class).toProvider(new SystemServiceProvider<LocationManager>(Context.LOCATION_SERVICE));
		bind(NotificationManager.class).toProvider(
				new SystemServiceProvider<NotificationManager>(Context.NOTIFICATION_SERVICE));
		bind(PowerManager.class).toProvider(new SystemServiceProvider<PowerManager>(Context.POWER_SERVICE));
		bind(SensorManager.class).toProvider(new SystemServiceProvider<SensorManager>(Context.SENSOR_SERVICE));
		bind(TelephonyManager.class).toProvider(new SystemServiceProvider<TelephonyManager>(Context.TELEPHONY_SERVICE));
		bind(Vibrator.class).toProvider(new SystemServiceProvider<Vibrator>(Context.VIBRATOR_SERVICE));
		bind(WifiManager.class).toProvider(new SystemServiceProvider<WifiManager>(Context.WIFI_SERVICE));
		bind(WindowManager.class).toProvider(new SystemServiceProvider<WindowManager>(Context.WINDOW_SERVICE));

		bind(mAccountManagerClass).toProvider(AccountManagerProvider.class);

		bind(ObserverManager.class);
		bindProviderListener(new ObserverRegister());
	}

	protected <T> BindingBuilder<T> bind(Class<T> clazz) {
		checkState(mBindings != null, "The Bindings can only be used inside configure()");
		return new BindingBuilderImpl<T>(clazz, mBindings);
	}

	protected void bindProviderListener(ProviderListener providerListener) {
		mProviderListeners.register(providerListener);
	}

	protected void bindFieldListener(Class<? extends Annotation> annClass, FieldListener fieldListener) {
		mFieldListeners.register(annClass, fieldListener);
	}
}

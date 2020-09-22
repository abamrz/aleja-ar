package com.example.AlejaGuidanceSystem;

import android.util.Log;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {
	@Override
	protected Config getSessionConfiguration(Session session) {
		// turning off hand-icon that appears on start-up.
		//getPlaneDiscoveryController().setInstructionView(null);
		Config config = new Config(session);
		// ensuring that update-listener is called whenever the camera frame updates.
		config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
		config.setFocusMode(Config.FocusMode.AUTO);
		session.configure(config);
		this.getArSceneView().setupSession(session);

		// setting up database for augmented images.
		((MainActivity) getActivity()).setupDatabase(config, session);

		return config;
	}
}

package com.example.AlejaGuidanceSystem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.math.Vector3;
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
		//((MakePlanActivity) getActivity()).setupDatabase(config, session);
		setupDatabase(config, session);

		return config;
	}

	private void setupDatabase(Config config, Session session) {
		// test image
		Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern1);
		Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern2);
		Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.ar_pattern3);

		AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
		// adding Augmented Images to Database
		aid.addImage("ar_pattern1", bitmap1, 0.16f);
		aid.addImage("ar_pattern2", bitmap2, 0.16f);
		aid.addImage("ar_pattern3", bitmap3, 0.16f);
		// aid.addImage("dr_christian_rehn", bitmap2, 0.2f);

		config.setAugmentedImageDatabase(aid);
	}
}



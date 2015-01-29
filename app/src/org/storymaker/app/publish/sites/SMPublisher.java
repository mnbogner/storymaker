package org.storymaker.app.publish.sites;

import android.content.Context;
import android.util.Log;

import org.storymaker.app.model.Auth;
import org.storymaker.app.model.Job;
import org.storymaker.app.model.JobTable;
import org.storymaker.app.model.PublishJob;
import org.storymaker.app.publish.PublishController;
import org.storymaker.app.publish.PublisherBase;

import io.scal.secureshareui.lib.FlickrBaseEncoder;

public class SMPublisher extends PublisherBase {
    private final String TAG = "SMPublisher";

    public SMPublisher(Context context, PublishController publishController, PublishJob publishJob) {
        super(context, publishController, publishJob);
    }

    @Override
    public void startRender() {
        Log.d(TAG, "startRender()");
        
        Job videoRenderJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_RENDER, null, VideoRenderer.SPEC_KEY);
        mController.enqueueJob(videoRenderJob);
    }

    @Override
    public void startUpload() {
        Log.d(TAG, "startUpload()");
        
        Job newJob = new Job(mContext, mPublishJob.getProjectId(), mPublishJob.getId(), JobTable.TYPE_UPLOAD, Auth.SITE_SM, null);
        mController.enqueueJob(newJob);
    }

    @Override
    public String getEmbed(Job job) {
        return "[storymaker " + job.getResult() + "]";
    }

    @Override
    public String getResultUrl(Job job) {
        /*
        Long id = Long.valueOf(job.getResult());
        String base58 = FlickrBaseEncoder.encode(id);
        return "https://flic.kr/p/" + base58;
        */

        // result from job should be url of post?
        return null;
    }
}

package org.smssecure.smssecure.mms;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.Registry;

import org.smssecure.smssecure.mms.ContactPhotoUriLoader.ContactPhotoUri;

import java.io.InputStream;

public class ContactPhotoUriLoader implements StreamModelLoader<ContactPhotoUri> {
  private final Context context;

  /**
   * THe default factory for {@link com.bumptech.glide.load.model.stream.StreamUriLoader}s.
   */
  public static class Factory implements ModelLoaderFactory<ContactPhotoUri, InputStream> {

    @Override
    public ModelLoader<ContactPhotoUri, InputStream> build(@NonNull Context context, @NonNull Registry registry) {
      return new ContactPhotoUriLoader(glide.getContext());
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }

  public ContactPhotoUriLoader(Context context) {
    this.context = context;
  }

  @Override
  public DataFetcher<InputStream> getResourceFetcher(ContactPhotoUri model, int width, int height) {
    return new ContactPhotoLocalUriFetcher(context, model.uri);
  }

  public static class ContactPhotoUri {
    public @NonNull Uri uri;

    public ContactPhotoUri(@NonNull Uri uri) {
      this.uri = uri;
    }
  }
}


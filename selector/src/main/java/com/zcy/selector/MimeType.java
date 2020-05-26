package com.zcy.selector;

import android.content.ContentResolver;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.StringDef;

import com.zcy.selector.internal.utils.PhotoMetadataUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Locale;


public final class MimeType {

    @StringDef({
            Type.JPEG,
            Type.PNG,
            Type.GIF,
            Type.BMP,
            Type.WEBP,
            Type.MPEG,
            Type.MP4,
            Type.QUICKTIME,
            Type.THREEGPP,
            Type.THREEGPP2,
            Type.MKV,
            Type.WEBM,
            Type.TS,
            Type.AVI
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        String JPEG = "image/jpeg";
        String PNG = "image/png";
        String GIF = "image/gif";
        String BMP = "image/x-ms-bmp";
        String WEBP = "image/webp";
        String MPEG = "video/mpeg";
        String MP4 = "video/mp4";
        String QUICKTIME = "video/quicktime";
        String THREEGPP = "video/3gpp";
        String THREEGPP2 = "video/3gpp2";
        String MKV = "video/x-matroska";
        String WEBM = "video/webm";
        String TS = "video/mp2ts";
        String AVI = "video/avi";
    }

    private final @Type
    String mMimeType;
    private final String[] mExtensions;

    public MimeType(@Type String mimeType, String... extensions) {
        mMimeType = mimeType;
        mExtensions = extensions;
    }

    public static MimeType[] of(@Type String mimeType) {
        switch (mimeType) {
            case Type.PNG:
                return new MimeType[]{new MimeType(Type.PNG, "png")};
            case Type.JPEG:
                return new MimeType[]{
                        new MimeType(Type.JPEG, "jpg", "jpeg")
                };
            case Type.GIF:
                return new MimeType[]{
                        new MimeType(Type.GIF, "gif")
                };
            case Type.BMP:
                return new MimeType[]{
                        new MimeType(Type.BMP, "bmp")
                };
            case Type.WEBP:
                return new MimeType[]{
                        new MimeType(Type.WEBP, "webp")
                };
            case Type.MPEG:
                return new MimeType[]{
                        new MimeType(Type.MPEG, "mpeg", "mpg")
                };
            case Type.MP4:
                return new MimeType[]{
                        new MimeType(Type.MP4, "mp4", "m4v")
                };
            case Type.QUICKTIME:
                return new MimeType[]{
                        new MimeType(Type.QUICKTIME, "mov")
                };
            case Type.THREEGPP:
                return new MimeType[]{
                        new MimeType(Type.THREEGPP, "3gp", "3gpp")
                };
            case Type.THREEGPP2:
                return new MimeType[]{
                        new MimeType(Type.THREEGPP2, "3g2", "3gpp2")
                };
            case Type.MKV:
                return new MimeType[]{
                        new MimeType(Type.MKV, "mkv")
                };
            case Type.WEBM:
                return new MimeType[]{
                        new MimeType(Type.WEBM, "webm")
                };
            case Type.TS:
                return new MimeType[]{
                        new MimeType(Type.TS, "ts")
                };
            case Type.AVI:
                return new MimeType[]{
                        new MimeType(Type.AVI, "avi")
                };
            default:
                return null;
        }
    }


    public static MimeType[] ofImage() {
        return new MimeType[]{
                new MimeType(Type.JPEG, "jpg", "jpeg"),
                new MimeType(Type.PNG, "png"),
                new MimeType(Type.GIF, "gif"),
                new MimeType(Type.BMP, "bmp"),
                new MimeType(Type.WEBP, "webp")
        };
    }

    public static MimeType[] ofImage(boolean onlyGif) {
        return new MimeType[]{
                new MimeType(Type.GIF, "gif")
        };
    }

    public static MimeType[] ofGif() {
        return ofImage(true);
    }

    public static MimeType[] ofVideo() {
        return new MimeType[]{
                new MimeType(Type.MPEG, "mpeg", "mpg"),
                new MimeType(Type.MP4, "mp4", "m4v"),
                new MimeType(Type.QUICKTIME, "mov"),
                new MimeType(Type.THREEGPP, "3gp", "3gpp"),
                new MimeType(Type.THREEGPP2, "3g2", "3gpp2"),
                new MimeType(Type.MKV, "mkv"),
                new MimeType(Type.WEBM, "webm"),
                new MimeType(Type.TS, "ts"),
                new MimeType(Type.AVI, "avi"),
        };
    }

    public static boolean isImage(@Type String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return mimeType.startsWith("image");
    }

    public static boolean isVideo(@Type String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return mimeType.startsWith("video");
    }

    public static boolean isGif(@Type String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return mimeType.equals(Type.GIF);
    }

    @Override
    public String toString() {
        return "MimeType{" +
                "mMimeType='" + mMimeType + '\'' +
                ", mExtensions=" + Arrays.toString(mExtensions) +
                '}';
    }

    public boolean checkType(ContentResolver resolver, Uri uri) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        if (uri == null) {
            return false;
        }
        String type = map.getExtensionFromMimeType(resolver.getType(uri));
        String path = null;
        boolean pathParsed = false;
        for (String extension : mExtensions) {
            if (extension.equals(type)) {
                return true;
            }
            if (!pathParsed) {
                path = PhotoMetadataUtils.getPath(resolver, uri);
                if (!TextUtils.isEmpty(path)) {
                    path = path.toLowerCase(Locale.US);
                }
                pathParsed = true;
            }
            if (path != null && path.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}

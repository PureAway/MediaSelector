package com.zcy.selector.internal.entity;

public class CaptureStrategy {

    private final boolean isPublic;
    private final String authority;
    private final String directory;

    public CaptureStrategy(boolean isPublic, String authority) {
        this(isPublic, authority, null);
    }

    public CaptureStrategy(boolean isPublic, String authority, String directory) {
        this.isPublic = isPublic;
        this.authority = authority;
        this.directory = directory;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDirectory() {
        return directory;
    }
}

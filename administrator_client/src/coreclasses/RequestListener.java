package coreclasses;

import coreclasses.dataclasses.Request;

public interface RequestListener {
    void onReceived(Request request);
}

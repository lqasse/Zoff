package no.lqasse.zoff.Interfaces;

/**
 * Created by lassedrevland on 24.03.15.
 */
public interface ZoffListener {
    public void zoffRefreshed(Boolean hasInetAccess);

    public void viewersChanged();
}

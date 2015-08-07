package topwise.com.aysnctasktest;

import android.os.AsyncTask;

/**
 * Created by lingyang on 8/7/15.
 */
public class ProgressAsyncTask extends AsyncTask<Integer, Integer, Double>
{
    /**
     * Creates a new asynchronous task. This constructor must be invoked on the UI thread.
     */
    public ProgressAsyncTask() {
        super();
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param integers The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Double doInBackground(Integer... integers) {
        int sum = 0;
        int max = integers[0].intValue();
        for (int i = 0; i <= max; i++) {
            sum += i;
            try {
                Thread.sleep(1000);        //sleep 1s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isCancelled() == true) break;
            int pregressValue = i / max;
            publishProgress(new Integer(pregressValue));
        }



        return new Double(max);
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param aVoid The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Double aVoid) {

        super.onPostExecute(aVoid);
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
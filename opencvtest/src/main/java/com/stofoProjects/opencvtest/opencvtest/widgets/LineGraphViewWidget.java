package com.stofoProjects.opencvtest.opencvtest.widgets;

import android.content.Context;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.models.MeanSegment;
import com.stofoProjects.opencvtest.opencvtest.utils.DataUtils;
import com.stofoProjects.opencvtest.opencvtest.utils.LogUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin Stofanak on 11.5.2014.
 */
public class LineGraphViewWidget {

    private static final String TAG = LogUtils.makeLogTag(LineGraphViewWidget.class);
    private static final int DEFAULT_THICKNESS = 1;
    private static final int DEFAULT_AVERAGE_SEGMENTS = 4;


    private int mVectorType;

    private GraphView mGraphView;
    private ViewGroup mGraphContainer;
    private GraphViewSeries mGraphSeries;
    private String mGraphTitle;
    private GraphView.GraphViewData[] mGraphData;
    private boolean mShowAverage = true;
    private int mAverageSegments = DEFAULT_AVERAGE_SEGMENTS;
    private GraphView.GraphViewData[] mGraphDataAverage;
    private GraphViewSeries mGraphAverageSeries;
    private int mGraphDataColor;
    private int mGraphAverageColor;


    public LineGraphViewWidget(Context context, String graphTitle, ViewGroup graphContainer, boolean showAverage) {

        mGraphView = new LineGraphView(context, graphTitle);
        mGraphTitle = graphTitle;
        mGraphContainer = graphContainer;

        mGraphContainer.addView(mGraphView);

        mGraphAverageColor = context.getResources().getColor(R.color.red);
        mShowAverage = showAverage;
    }

    /**
     *
     * @param vectorData vector of data to show in graph one row many columns
     */
    public void updateGraph(Mat vectorData) {
        final int dataCount;
        if(vectorData.width() == 1) {
            mVectorType = DataUtils.COLUMN_VECTOR;
            dataCount = vectorData.height();
        } else {
            mVectorType = DataUtils.ROW_VECTOR;
            dataCount = vectorData.width();
        }

        if(mGraphData == null || mGraphSeries == null) {
            initGraphData(dataCount);
        }

        for(int i = 0; i < dataCount; i++) {
            mGraphData[i] = mVectorType == DataUtils.ROW_VECTOR
                            ? new GraphView.GraphViewData(i, vectorData.get(0, i)[0])
                            : new GraphView.GraphViewData(i, vectorData.get(i, 0)[0]);
        }

        //Calculate mean for all segments in vector data
        if(mShowAverage) {
            final List<MeanSegment> averages = countAverageSegments(vectorData);

            for(int i = 0; i < mAverageSegments; i++) {
                mGraphDataAverage[i * 2] = new GraphView.GraphViewData(
                                                                averages.get(i).getStart()
                                                                , averages.get(i).getMeanValue()
                );
                mGraphDataAverage[(i * 2) + 1] = new GraphView.GraphViewData(
                                                                    averages.get(i).getEnd()
                                                                    , averages.get(i).getMeanValue()
                );

            }

            mGraphAverageSeries.resetData(mGraphDataAverage);
        }

        mGraphSeries.resetData(mGraphData);
    }

    /**
     * Initialize GraphData array, grapSeries and attach series to graphView
     * @param dataCount
     */
    private void initGraphData(int dataCount){
        mGraphData = new GraphView.GraphViewData[dataCount];
        mGraphSeries = new GraphViewSeries(mGraphData);
        mGraphView.addSeries(mGraphSeries);

        if(mShowAverage) {
            mGraphDataAverage = new GraphView.GraphViewData[mAverageSegments * 2];
            mGraphAverageSeries = new GraphViewSeries("Average", getGraphStyleFromColor(mGraphAverageColor), mGraphDataAverage);
            mGraphView.addSeries(mGraphAverageSeries);
        }
    }

    /**
     * Calculate mean for more segments of vector data
     * @param vectorData whole vector data, which function will segment for calculation
     * @return list of calculated means objects
     */
    private List<MeanSegment> countAverageSegments(Mat vectorData) {
        final int segmentLength = (mVectorType == DataUtils.COLUMN_VECTOR)
                                    ? vectorData.height() / mAverageSegments
                                    : vectorData.width() / mAverageSegments;
        List<MeanSegment> averages = new ArrayList<MeanSegment>(mAverageSegments);

        for(int i = 0; i < mAverageSegments; i++) {
            int start = i * segmentLength;
            int end = ((i + 1) * segmentLength) - 1;
            Mat segmentVector;
            if(mVectorType == DataUtils.COLUMN_VECTOR)
                segmentVector = vectorData.rowRange(start, end);
            else
                segmentVector = vectorData.colRange(start, end);

            averages.add(new MeanSegment(start, end, countAverage(segmentVector)));
        }
        return averages;
    }

    /**
     * Count average value from Mat one row vector
     * @param vectorData vector
     * @return average value
     */
    private double countAverage(Mat vectorData) {
        Scalar mean = Core.mean(vectorData);
        return mean.val[0];
    }

    private GraphViewSeries.GraphViewSeriesStyle getGraphStyleFromColor(int color) {
        return  new GraphViewSeries.GraphViewSeriesStyle(color, DEFAULT_THICKNESS);
    }



    public void setNumberOfAverageSegments(int mAverageSegments) {
        this.mAverageSegments = mAverageSegments;
    }
}

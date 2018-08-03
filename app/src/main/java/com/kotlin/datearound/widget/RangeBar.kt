package com.kotlin.datearound.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.kotlin.datearound.R
import java.util.*

class RangeBar(context: Context) : View(context) {

    // Member Variables ////////////////////////////////////////////////////////

    private val TAG = "RangeBar"

    // Default values for variables
    private val DEFAULT_TICK_START = 0f

    private val DEFAULT_TICK_END = 5f

    private val DEFAULT_TICK_INTERVAL = 1f

    private val DEFAULT_TICK_HEIGHT_DP = 1f

    private val DEFAULT_PIN_PADDING_DP = 16f

    val DEFAULT_MIN_PIN_FONT_SP = 8f

    val DEFAULT_MAX_PIN_FONT_SP = 24f

    private val DEFAULT_BAR_WEIGHT_DP = 2f

    private val DEFAULT_CIRCLE_BOUNDARY_SIZE_DP = 0f

    private val DEFAULT_BAR_COLOR = Color.LTGRAY

    private val DEFAULT_TEXT_COLOR = Color.WHITE

    private val DEFAULT_TICK_COLOR = Color.BLACK

    // Corresponds to material indigo 500.
    private val DEFAULT_PIN_COLOR = -0xc0ae4b

    private val DEFAULT_CONNECTING_LINE_WEIGHT_DP = 4f

    // Corresponds to material indigo 500.
    private val DEFAULT_CONNECTING_LINE_COLOR = -0xc0ae4b

    private val DEFAULT_EXPANDED_PIN_RADIUS_DP = 12f

    private val DEFAULT_CIRCLE_SIZE_DP = 5f

    private val DEFAULT_BAR_PADDING_BOTTOM_DP = 24f

    // Instance variables for all of the customizable attributes

    private var mTickHeight = DEFAULT_TICK_HEIGHT_DP

    private var mTickStart = DEFAULT_TICK_START

    private var mTickEnd = DEFAULT_TICK_END

    private var mTickInterval = DEFAULT_TICK_INTERVAL

    private var mBarWeight = DEFAULT_BAR_WEIGHT_DP

    private var mIsBarRounded = false

    private var mBarColor = DEFAULT_BAR_COLOR

    private var mPinColor = DEFAULT_PIN_COLOR

    private var mTextColor = DEFAULT_TEXT_COLOR

    private var mConnectingLineWeight = DEFAULT_CONNECTING_LINE_WEIGHT_DP

    private var mConnectingLineColor = DEFAULT_CONNECTING_LINE_COLOR

    private var mThumbRadiusDP = DEFAULT_EXPANDED_PIN_RADIUS_DP

    private var mTickColor = DEFAULT_TICK_COLOR

    private var mExpandedPinRadius = DEFAULT_EXPANDED_PIN_RADIUS_DP

    private var mCircleColor = DEFAULT_CONNECTING_LINE_COLOR

    private var mCircleBoundaryColor = DEFAULT_CONNECTING_LINE_COLOR

    private var mCircleBoundarySize = DEFAULT_CIRCLE_BOUNDARY_SIZE_DP

    private var mCircleSize = DEFAULT_CIRCLE_SIZE_DP

    private var mMinPinFont = DEFAULT_MIN_PIN_FONT_SP

    private var mMaxPinFont = DEFAULT_MAX_PIN_FONT_SP

    // setTickCount only resets indices before a thumb has been pressed or a
    // setThumbIndices() is called, to correspond with intended usage
    private var mFirstSetTickCount = true

    private val mDisplayMetrices = getResources().getDisplayMetrics()

    private val mDefaultWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f, mDisplayMetrices).toInt()

    private val mDefaultHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75f, mDisplayMetrices).toInt()

    private var mTickCount = ((mTickEnd - mTickStart) / mTickInterval).toInt() + 1

    private lateinit var mLeftThumb: PinView

    private lateinit var mRightThumb: PinView

    private var mBar: Bar? = null

    private lateinit var mConnectingLine: ConnectingLine

    private var mListener: OnRangeBarChangeListener? = null

    private var mPinTextListener: OnRangeBarTextListener? = null

    private var mTickMap: HashMap<Float, String>? = null

    private var mLeftIndex: Int = 0

    private var mRightIndex: Int = 0

    private var mIsRangeBar = true

    private var mPinPadding = DEFAULT_PIN_PADDING_DP

    private var mBarPaddingBottom = DEFAULT_BAR_PADDING_BOTTOM_DP

    private var mActiveConnectingLineColor: Int = 0

    private var mActiveBarColor: Int = 0

    private var mActiveTickColor: Int = 0

    private var mActiveCircleColor: Int = 0

    //Used for ignoring vertical moves
    private var mDiffX: Int = 0

    private var mDiffY: Int = 0

    private var mLastX: Float = 0.toFloat()

    private var mLastY: Float = 0.toFloat()

    private var mFormatter: IRangeBarFormatter? = null

    private var drawTicks = true

    private var mArePinsTemporary = true

    private var mPinTextFormatter: PinTextFormatter = object : PinTextFormatter {
        override fun getText(value: String): String {
            return if (value.length > 4) {
                value.substring(0, 4)
            } else {
                value
            }
        }
    }

    // Constructors ////////////////////////////////////////////////////////////

    fun RangeBar(context: Context) {
       // super(context)
    }

    fun RangeBar(context: Context, attrs: AttributeSet) {
       // super(context, attrs)
        rangeBarInit(context, attrs)
    }

    fun RangeBar(context: Context, attrs: AttributeSet, defStyle: Int) {
       // super(context, attrs, defStyle)
        rangeBarInit(context, attrs)
    }

    // View Methods ////////////////////////////////////////////////////////////

    override fun onSaveInstanceState(): Parcelable? {

        val bundle = Bundle()

        bundle.putParcelable("instanceState", super.onSaveInstanceState())

        bundle.putInt("TICK_COUNT", mTickCount)
        bundle.putFloat("TICK_START", mTickStart)
        bundle.putFloat("TICK_END", mTickEnd)
        bundle.putFloat("TICK_INTERVAL", mTickInterval)
        bundle.putInt("TICK_COLOR", mTickColor)

        bundle.putFloat("TICK_HEIGHT_DP", mTickHeight)
        bundle.putFloat("BAR_WEIGHT", mBarWeight)
        bundle.putBoolean("BAR_ROUNDED", mIsBarRounded)
        bundle.putInt("BAR_COLOR", mBarColor)
        bundle.putFloat("CONNECTING_LINE_WEIGHT", mConnectingLineWeight)
        bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor)

        bundle.putFloat("CIRCLE_SIZE", mCircleSize)
        bundle.putInt("CIRCLE_COLOR", mCircleColor)
        bundle.putInt("CIRCLE_BOUNDARY_COLOR", mCircleBoundaryColor)
        bundle.putFloat("CIRCLE_BOUNDARY_WIDTH", mCircleBoundarySize)
        bundle.putFloat("THUMB_RADIUS_DP", mThumbRadiusDP)
        bundle.putFloat("EXPANDED_PIN_RADIUS_DP", mExpandedPinRadius)
        bundle.putFloat("PIN_PADDING", mPinPadding)
        bundle.putFloat("BAR_PADDING_BOTTOM", mBarPaddingBottom)
        bundle.putBoolean("IS_RANGE_BAR", mIsRangeBar)
        bundle.putBoolean("ARE_PINS_TEMPORARY", mArePinsTemporary)
        bundle.putInt("LEFT_INDEX", mLeftIndex)
        bundle.putInt("RIGHT_INDEX", mRightIndex)

        bundle.putBoolean("FIRST_SET_TICK_COUNT", mFirstSetTickCount)

        bundle.putFloat("MIN_PIN_FONT", mMinPinFont)
        bundle.putFloat("MAX_PIN_FONT", mMaxPinFont)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        if (state is Bundle) {

            val bundle = state as Bundle

            mTickCount = bundle.getInt("TICK_COUNT")
            mTickStart = bundle.getFloat("TICK_START")
            mTickEnd = bundle.getFloat("TICK_END")
            mTickInterval = bundle.getFloat("TICK_INTERVAL")
            mTickColor = bundle.getInt("TICK_COLOR")
            mTickHeight = bundle.getFloat("TICK_HEIGHT_DP")
            mBarWeight = bundle.getFloat("BAR_WEIGHT")
            mIsBarRounded = bundle.getBoolean("BAR_ROUNDED", false)
            mBarColor = bundle.getInt("BAR_COLOR")
            mCircleSize = bundle.getFloat("CIRCLE_SIZE")
            mCircleColor = bundle.getInt("CIRCLE_COLOR")
            mCircleBoundaryColor = bundle.getInt("CIRCLE_BOUNDARY_COLOR")
            mCircleBoundarySize = bundle.getFloat("CIRCLE_BOUNDARY_WIDTH")
            mConnectingLineWeight = bundle.getFloat("CONNECTING_LINE_WEIGHT")
            mConnectingLineColor = bundle.getInt("CONNECTING_LINE_COLOR")

            mThumbRadiusDP = bundle.getFloat("THUMB_RADIUS_DP")
            mExpandedPinRadius = bundle.getFloat("EXPANDED_PIN_RADIUS_DP")
            mPinPadding = bundle.getFloat("PIN_PADDING")
            mBarPaddingBottom = bundle.getFloat("BAR_PADDING_BOTTOM")
            mIsRangeBar = bundle.getBoolean("IS_RANGE_BAR")
            mArePinsTemporary = bundle.getBoolean("ARE_PINS_TEMPORARY")

            mLeftIndex = bundle.getInt("LEFT_INDEX")
            mRightIndex = bundle.getInt("RIGHT_INDEX")
            mFirstSetTickCount = bundle.getBoolean("FIRST_SET_TICK_COUNT")

            mMinPinFont = bundle.getFloat("MIN_PIN_FONT")
            mMaxPinFont = bundle.getFloat("MAX_PIN_FONT")

            setRangePinsByIndices(mLeftIndex, mRightIndex)
            super.onRestoreInstanceState(bundle.getParcelable<Parcelable>("instanceState"))

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val width: Int
        val height: Int

        // Get measureSpec mode and size values.
        val measureWidthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        // The RangeBar width should be as large as possible.
        if (measureWidthMode == View.MeasureSpec.AT_MOST) {
            width = measureWidth
        } else if (measureWidthMode == View.MeasureSpec.EXACTLY) {
            width = measureWidth
        } else {
            width = mDefaultWidth
        }

        // The RangeBar height should be as small as possible.
        if (measureHeightMode == View.MeasureSpec.AT_MOST) {
            height = Math.min(mDefaultHeight, measureHeight)
        } else if (measureHeightMode == View.MeasureSpec.EXACTLY) {
            height = measureHeight
        } else {
            height = mDefaultHeight
        }

        setMeasuredDimension(width, height)
    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

//        super.onSizeChanged(w, h, oldw, oldh)

        val ctx = context

        // This is the initial point at which we know the size of the View.

        // Create the two thumb objects and position line in view
        val density = mDisplayMetrices.density
        val expandedPinRadius = mExpandedPinRadius / density

        val yPos = h - mBarPaddingBottom
        if (mIsRangeBar) {
            mLeftThumb = PinView(ctx)
            mLeftThumb!!.setFormatter(mFormatter)
            mLeftThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mCircleSize,
                    mCircleColor, mCircleBoundaryColor, mCircleBoundarySize, mMinPinFont, mMaxPinFont, mArePinsTemporary)
        }
        mRightThumb = PinView(ctx)
        mRightThumb!!.setFormatter(mFormatter)
        mRightThumb!!.init(ctx, yPos, expandedPinRadius, mPinColor, mTextColor, mCircleSize,
                mCircleColor, mCircleBoundaryColor, mCircleBoundarySize, mMinPinFont, mMaxPinFont, mArePinsTemporary)

        // Create the underlying bar.
        val marginLeft = Math.max(mExpandedPinRadius, mCircleSize)

        val barLength = w - 2 * marginLeft
        mBar = Bar(ctx, marginLeft, yPos, barLength, mTickCount, mTickHeight, mTickColor,
                mBarWeight, mBarColor, mIsBarRounded)

        // Initialize thumbs to the desired indices
        if (mIsRangeBar) {
            mLeftThumb!!.x = marginLeft + (mLeftIndex / (mTickCount - 1).toFloat()) * barLength
            mLeftThumb!!.setXValue(getPinValue(mLeftIndex))
        }
        mRightThumb!!.x = marginLeft + (mRightIndex / (mTickCount - 1).toFloat()) * barLength
        mRightThumb!!.setXValue(getPinValue(mRightIndex))

        // Set the thumb indices.
        val newLeftIndex = if (mIsRangeBar) mBar!!.getNearestTickIndex(mLeftThumb) else 0
        val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb)

        // Call the listener.
        if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {
            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex),
                        getPinValue(mRightIndex))
            }
        }

        // Create the line connecting the two thumbs.
        mConnectingLine = ConnectingLine(ctx, yPos, mConnectingLineWeight,
                mConnectingLineColor)
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        mBar!!.draw(canvas)
        if (mIsRangeBar) {
            mConnectingLine!!.draw(canvas, mLeftThumb, mRightThumb)
            if (drawTicks) {
                mBar!!.drawTicks(canvas)
            }
            mLeftThumb!!.draw(canvas)
        } else {
            mConnectingLine!!.draw(canvas, getMarginLeft(), mRightThumb)
            if (drawTicks) {
                mBar!!.drawTicks(canvas)
            }
        }
        mRightThumb!!.draw(canvas)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If this View is not enabled, don't allow for touch interactions.
        if (!isEnabled()) {
            return false
        }

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                mDiffX = 0
                mDiffY = 0

                mLastX = event.x
                mLastY = event.y
                onActionDown(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_UP -> {
                this.getParent().requestDisallowInterceptTouchEvent(false)
                onActionUp(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                this.getParent().requestDisallowInterceptTouchEvent(false)
                onActionUp(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                onActionMove(event.x)
                this.getParent().requestDisallowInterceptTouchEvent(true)
                val curX = event.x
                val curY = event.y
                mDiffX += Math.abs(curX - mLastX).toInt()
                mDiffY += Math.abs(curY - mLastY).toInt()
                mLastX = curX
                mLastY = curY

                if (mDiffX < mDiffY) {
                    //vertical touch
                    getParent().requestDisallowInterceptTouchEvent(false)
                    return false
                } else {
                    //horizontal touch (do nothing as it is needed for RangeBar)
                }
                return true
            }

            else -> return false
        }
    }

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Sets a listener to receive notifications of changes to the RangeBar. This
     * will overwrite any existing set listeners.
     *
     * @param listener the RangeBar notification listener; null to remove any
     * existing listener
     */
    fun setOnRangeBarChangeListener(listener: OnRangeBarChangeListener) {
        mListener = listener
    }

    /**
     * Sets a listener to modify the text
     *
     * @param mPinTextListener the RangeBar pin text notification listener; null to remove any
     * existing listener
     */
    fun setPinTextListener(mPinTextListener: OnRangeBarTextListener) {
        this.mPinTextListener = mPinTextListener
    }


    fun setFormatter(formatter: IRangeBarFormatter) {
        if (mLeftThumb != null) {
            mLeftThumb!!.setFormatter(formatter)
        }

        if (mRightThumb != null) {
            mRightThumb!!.setFormatter(formatter)
        }

        mFormatter = formatter
    }

    fun setDrawTicks(drawTicks: Boolean) {
        this.drawTicks = drawTicks
    }

    /**
     * Sets the start tick in the RangeBar.
     *
     * @param tickStart Integer specifying the number of ticks.
     */
    fun setTickStart(tickStart: Float) {
        val tickCount = ((mTickEnd - tickStart) / mTickInterval).toInt() + 1
        if (isValidTickCount(tickCount)) {
            mTickCount = tickCount
            mTickStart = tickStart

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex),
                            getPinValue(mRightIndex))
                }
            }
            if (indexOutOfRange(mLeftIndex, mRightIndex)) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex),
                            getPinValue(mRightIndex))
                }
            }

            createBar()
            createPins()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the start tick in the RangeBar.
     *
     * @param tickInterval Integer specifying the number of ticks.
     */
    fun setTickInterval(tickInterval: Float) {
        val tickCount = ((mTickEnd - mTickStart) / tickInterval).toInt() + 1
        if (isValidTickCount(tickCount)) {
            mTickCount = tickCount
            mTickInterval = tickInterval

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex), getPinValue(mRightIndex))
                }
            }
            if (indexOutOfRange(mLeftIndex, mRightIndex)) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex), getPinValue(mRightIndex))
                }
            }

            createBar()
            createPins()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the end tick in the RangeBar.
     *
     * @param tickEnd Integer specifying the number of ticks.
     */
    fun setTickEnd(tickEnd: Float) {
        val tickCount = ((tickEnd - mTickStart) / mTickInterval).toInt() + 1
        if (isValidTickCount(tickCount)) {
            mTickCount = tickCount
            mTickEnd = tickEnd

            // Prevents resetting the indices when creating new activity, but
            // allows it on the first setting.
            if (mFirstSetTickCount) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex), getPinValue(mRightIndex))
                }
            }
            if (indexOutOfRange(mLeftIndex, mRightIndex)) {
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex), getPinValue(mRightIndex))
                }
            }

            createBar()
            createPins()
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.")
            throw IllegalArgumentException("tickCount less than 2; invalid tickCount.")
        }
    }

    /**
     * Sets the height of the ticks in the range bar.
     *
     * @param tickHeight Float specifying the height of each tick mark in dp.
     */
    fun setTickHeight(tickHeight: Float) {

        mTickHeight = tickHeight
        createBar()
    }

    /**
     * Set the weight of the bar line and the tick lines in the range bar.
     *
     * @param barWeight Float specifying the weight of the bar and tick lines in
     * DP.
     */
    fun setBarWeight(barWeight: Float) {

        mBarWeight = barWeight
        createBar()
    }

    fun isBarRounded(): Boolean {
        return mIsBarRounded
    }

    /**
     * set the bar with rounded corners
     * @param isBarRounded flag
     */
    fun setBarRounded(isBarRounded: Boolean) {
        this.mIsBarRounded = isBarRounded
        createBar()
    }

    /**
     * Set the color of the bar line and the tick lines in the range bar.
     *
     * @param barColor Integer specifying the color of the bar line.
     */
    fun setBarColor(barColor: Int) {
        mBarColor = barColor
        createBar()
    }

    /**
     * Set the color of the pins.
     *
     * @param pinColor Integer specifying the color of the pin.
     */
    fun setPinColor(pinColor: Int) {
        mPinColor = pinColor
        createPins()
    }

    /**
     * Set the color of the text within the pin.
     *
     * @param textColor Integer specifying the color of the text in the pin.
     */
    fun setPinTextColor(textColor: Int) {
        mTextColor = textColor
        createPins()
    }

    /**
     * Set if the view is a range bar or a seek bar.
     *
     * @param isRangeBar Boolean - true sets it to rangebar, false to seekbar.
     */
    fun setRangeBarEnabled(isRangeBar: Boolean) {
        mIsRangeBar = isRangeBar
        invalidate()
    }


    /**
     * Set if the pins should dissapear after released
     *
     * @param arePinsTemporary Boolean - true if pins shoudl dissapear after released, false to
     * stay
     * drawn
     */
    fun setTemporaryPins(arePinsTemporary: Boolean) {
        mArePinsTemporary = arePinsTemporary
        invalidate()
    }


    /**
     * Set the color of the ticks.
     *
     * @param tickColor Integer specifying the color of the ticks.
     */
    fun setTickColor(tickColor: Int) {

        mTickColor = tickColor
        createBar()
    }

    /**
     * Set the color of the selector.
     *
     * @param selectorColor Integer specifying the color of the ticks.
     */
    fun setSelectorColor(selectorColor: Int) {
        mCircleColor = selectorColor
        createPins()
    }

    /**
     * Set the color of the selector Boundary.
     *
     * @param selectorBoundaryColor Integer specifying the boundary color of the ticks.
     */
    fun setSelectorBoundaryColor(selectorBoundaryColor: Int) {
        mCircleBoundaryColor = selectorBoundaryColor
        createPins()
    }

    /**
     * Set the size of the selector Boundary.
     *
     * @param selectorBoundarySize Integer specifying the boundary size of ticks.
     * Value should be in DP
     */
    fun setSelectorBoundarySize(selectorBoundarySize: Int) {
        mCircleBoundarySize = selectorBoundarySize.toFloat()
        createPins()
    }

    /**
     * Set the weight of the connecting line between the thumbs.
     *
     * @param connectingLineWeight Float specifying the weight of the connecting
     * line. Value should be in DP
     */
    fun setConnectingLineWeight(connectingLineWeight: Float) {

        mConnectingLineWeight = connectingLineWeight
        createConnectingLine()
    }

    /**
     * Set the color of the connecting line between the thumbs.
     *
     * @param connectingLineColor Integer specifying the color of the connecting
     * line.
     */
    fun setConnectingLineColor(connectingLineColor: Int) {

        mConnectingLineColor = connectingLineColor
        createConnectingLine()
    }

    /**
     * If this is set, the thumb images will be replaced with a circle of the
     * specified radius. Default width = 12dp.
     *
     * @param pinRadius Float specifying the radius of the thumbs to be drawn. Value should be in DP
     */
    fun setPinRadius(pinRadius: Float) {
        mExpandedPinRadius = pinRadius
        createPins()
    }

    /**
     * Gets the start tick.
     *
     * @return the start tick.
     */
    fun getTickStart(): Float {
        return mTickStart
    }

    /**
     * Gets the end tick.
     *
     * @return the end tick.
     */
    fun getTickEnd(): Float {
        return mTickEnd
    }

    /**
     * Gets the tick count.
     *
     * @return the tick count
     */
    fun getTickCount(): Int {
        return mTickCount
    }

    /**
     * Sets the location of the pins according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param leftPinIndex  Integer specifying the index of the left pin
     * @param rightPinIndex Integer specifying the index of the right pin
     */
    fun setRangePinsByIndices(leftPinIndex: Int, rightPinIndex: Int) {
        if (indexOutOfRange(leftPinIndex, rightPinIndex)) {
            Log.e(TAG,
                    "Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")")
            throw IllegalArgumentException(
                    ("Pin index left " + leftPinIndex + ", or right " + rightPinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")"))
        } else {

            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            mLeftIndex = leftPinIndex
            mRightIndex = rightPinIndex
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex), getPinValue(mRightIndex))
            }
        }

        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param pinIndex Integer specifying the index of the seek pin
     */
    fun setSeekPinByIndex(pinIndex: Int) {
        if (pinIndex < 0 || pinIndex > mTickCount) {
            Log.e(TAG,
                    ("Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + mTickCount + ")"))
            throw IllegalArgumentException(
                    ("Pin index " + pinIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + 0 + ") and less than the maximum value ("
                            + mTickCount + ")"))

        } else {

            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            mRightIndex = pinIndex
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex), getPinValue(mRightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pins according by the supplied values.
     *
     * @param leftPinValue  Float specifying the index of the left pin
     * @param rightPinValue Float specifying the index of the right pin
     */
    fun setRangePinsByValue(leftPinValue: Float, rightPinValue: Float) {
        if (valueOutOfRange(leftPinValue, rightPinValue)) {
            Log.e(TAG,
                    ("Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")"))
            throw IllegalArgumentException(
                    ("Pin value left " + leftPinValue + ", or right " + rightPinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")"))
        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }

            mLeftIndex = ((leftPinValue - mTickStart) / mTickInterval).toInt()
            mRightIndex = ((rightPinValue - mTickStart) / mTickInterval).toInt()
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex), getPinValue(mRightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Sets the location of pin according by the supplied value.
     *
     * @param pinValue Float specifying the value of the pin
     */
    fun setSeekPinByValue(pinValue: Float) {
        if (pinValue > mTickEnd || pinValue < mTickStart) {
            Log.e(TAG,
                    ("Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")"))
            throw IllegalArgumentException(
                    ("Pin value " + pinValue
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")"))

        } else {
            if (mFirstSetTickCount) {
                mFirstSetTickCount = false
            }
            mRightIndex = ((pinValue - mTickStart) / mTickInterval).toInt()
            createPins()

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex), getPinValue(mRightIndex))
            }
        }
        invalidate()
        requestLayout()
    }

    /**
     * Gets the type of the bar.
     *
     * @return true if rangebar, false if seekbar.
     */
    fun isRangeBar(): Boolean {
        return mIsRangeBar
    }

    /**
     * Gets the value of the left pin.
     *
     * @return the string value of the left pin.
     */
    fun getLeftPinValue(): String {
        return getPinValue(mLeftIndex)
    }

    /**
     * Gets the value of the right pin.
     *
     * @return the string value of the right pin.
     */
    fun getRightPinValue(): String {
        return getPinValue(mRightIndex)
    }

    /**
     * Gets the index of the left-most pin.
     *
     * @return the 0-based index of the left pin
     */
    fun getLeftIndex(): Int {
        return mLeftIndex
    }

    /**
     * Gets the index of the right-most pin.
     *
     * @return the 0-based index of the right pin
     */
    fun getRightIndex(): Int {
        return mRightIndex
    }

    /**
     * Gets the tick interval.
     *
     * @return the tick interval
     */
    fun getTickInterval(): Double {
        return mTickInterval.toDouble()
    }

     override fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            mBarColor = DEFAULT_BAR_COLOR
            mConnectingLineColor = DEFAULT_BAR_COLOR
            mCircleColor = DEFAULT_BAR_COLOR
            mTickColor = DEFAULT_BAR_COLOR
        } else {
            mBarColor = mActiveBarColor
            mConnectingLineColor = mActiveConnectingLineColor
            mCircleColor = mActiveCircleColor
            mTickColor = mActiveTickColor
        }

        createBar()
        createPins()
        createConnectingLine()
        super.setEnabled(enabled)
    }

    fun setPinTextFormatter(pinTextFormatter: PinTextFormatter) {
        this.mPinTextFormatter = pinTextFormatter
    }

    // Private Methods /////////////////////////////////////////////////////////

    /**
     * Does all the functions of the constructor for RangeBar. Called by both
     * RangeBar constructors in lieu of copying the code for each constructor.
     *
     * @param context Context from the constructor.
     * @param attrs   AttributeSet from the constructor.
     */
    private fun rangeBarInit(context: Context, attrs: AttributeSet) {
        //TODO tick value map
        if (mTickMap == null) {
            mTickMap = HashMap()
        }
        val ta = context.obtainStyledAttributes(attrs, R.styleable.RangeBar, 0, 0)

        try {

            // Sets the values of the user-defined attributes based on the XML
            // attributes.
            val tickStart = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickStart, DEFAULT_TICK_START)
            val tickEnd = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickEnd, DEFAULT_TICK_END)
            val tickInterval = ta
                    .getFloat(R.styleable.RangeBar_mrb_tickInterval, DEFAULT_TICK_INTERVAL)
            val tickCount = ((tickEnd - tickStart) / tickInterval).toInt() + 1
            if (isValidTickCount(tickCount)) {

                // Similar functions performed above in setTickCount; make sure
                // you know how they interact
                mTickCount = tickCount
                mTickStart = tickStart
                mTickEnd = tickEnd
                mTickInterval = tickInterval
                mLeftIndex = 0
                mRightIndex = mTickCount - 1

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex),
                            getPinValue(mRightIndex))
                }

            } else {

                Log.e(TAG, "tickCount less than 2; invalid tickCount. XML input ignored.")
            }

            mTickHeight = ta.getDimension(R.styleable.RangeBar_mrb_tickHeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TICK_HEIGHT_DP,
                            mDisplayMetrices)
            )
            mBarWeight = ta.getDimension(R.styleable.RangeBar_mrb_barWeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_WEIGHT_DP,
                            mDisplayMetrices)
            )
            mCircleSize = ta.getDimension(R.styleable.RangeBar_mrb_selectorSize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_SIZE_DP,
                            mDisplayMetrices)
            )
            mCircleBoundarySize = ta.getDimension(R.styleable.RangeBar_mrb_selectorBoundarySize,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_BOUNDARY_SIZE_DP,
                            mDisplayMetrices)
            )
            mConnectingLineWeight = ta.getDimension(R.styleable.RangeBar_mrb_connectingLineWeight,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CONNECTING_LINE_WEIGHT_DP,
                            mDisplayMetrices)
            )
            mExpandedPinRadius = ta.getDimension(R.styleable.RangeBar_mrb_pinRadius,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_EXPANDED_PIN_RADIUS_DP,
                            mDisplayMetrices)
            )
            mPinPadding = ta.getDimension(R.styleable.RangeBar_mrb_pinPadding,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PIN_PADDING_DP,
                            mDisplayMetrices)
            )
            mBarPaddingBottom = ta.getDimension(R.styleable.RangeBar_mrb_rangeBarPaddingBottom,
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BAR_PADDING_BOTTOM_DP,
                            mDisplayMetrices)
            )

            mBarColor = ta.getColor(R.styleable.RangeBar_mrb_rangeBarColor, DEFAULT_BAR_COLOR)
            mTextColor = ta.getColor(R.styleable.RangeBar_mrb_pinTextColor, DEFAULT_TEXT_COLOR)
            mPinColor = ta.getColor(R.styleable.RangeBar_mrb_pinColor, DEFAULT_PIN_COLOR)
            mActiveBarColor = mBarColor


            mCircleColor = ta.getColor(R.styleable.RangeBar_mrb_selectorColor,
                    DEFAULT_CONNECTING_LINE_COLOR)
            mCircleBoundaryColor = ta.getColor(R.styleable.RangeBar_mrb_selectorBoundaryColor,
                    DEFAULT_CONNECTING_LINE_COLOR)


            mActiveCircleColor = mCircleColor
            mTickColor = ta.getColor(R.styleable.RangeBar_mrb_tickColor, DEFAULT_TICK_COLOR)
            mActiveTickColor = mTickColor

            mConnectingLineColor = ta.getColor(R.styleable.RangeBar_mrb_connectingLineColor,
                    DEFAULT_CONNECTING_LINE_COLOR)
            mActiveConnectingLineColor = mConnectingLineColor


            mIsRangeBar = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar, true)
            mArePinsTemporary = ta.getBoolean(R.styleable.RangeBar_mrb_temporaryPins, true)
            mIsBarRounded = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar_rounded, false)

            val density = mDisplayMetrices.density
            mMinPinFont = ta.getDimension(R.styleable.RangeBar_mrb_pinMinFont,
                    DEFAULT_MIN_PIN_FONT_SP * density)
            mMaxPinFont = ta.getDimension(R.styleable.RangeBar_mrb_pinMaxFont,
                    DEFAULT_MAX_PIN_FONT_SP * density)

            mIsRangeBar = ta.getBoolean(R.styleable.RangeBar_mrb_rangeBar, true)
        } finally {
            ta.recycle()
        }
    }

    /**
     * Creates a new mBar
     */
    private fun createBar() {
        mBar = Bar(getContext(),
                getMarginLeft(),
                getYPos(),
                getBarLength(),
                mTickCount,
                mTickHeight,
                mTickColor,
                mBarWeight,
                mBarColor,
                mIsBarRounded)
        invalidate()
    }

    /**
     * Creates a new ConnectingLine.
     */
    private fun createConnectingLine() {

        mConnectingLine = ConnectingLine(getContext(),
                getYPos(),
                mConnectingLineWeight,
                mConnectingLineColor)
        invalidate()
    }

    /**
     * Creates two new Pins.
     */
    private fun createPins() {
        val ctx = getContext()
        val yPos = getYPos()

        if (mIsRangeBar) {
            mLeftThumb = PinView(ctx)
            mLeftThumb.init(ctx, yPos, 0F, mPinColor, mTextColor, mCircleSize, mCircleColor, mCircleBoundaryColor, mCircleBoundarySize,
                    mMinPinFont, mMaxPinFont, false)
        }
        mRightThumb = PinView(ctx)
        mRightThumb
                .init(ctx, yPos, 0F, mPinColor, mTextColor, mCircleSize, mCircleColor, mCircleBoundaryColor, mCircleBoundarySize, mMinPinFont, mMaxPinFont, false)

        val marginLeft = getMarginLeft()
        val barLength = getBarLength()

        // Initialize thumbs to the desired indices
        if (mIsRangeBar) {
            mLeftThumb.x = marginLeft + (mLeftIndex / (mTickCount - 1).toFloat()) * barLength
            mLeftThumb.setXValue(getPinValue(mLeftIndex))
        }
        mRightThumb.x = marginLeft + (mRightIndex / (mTickCount - 1).toFloat()) * barLength
        mRightThumb.setXValue(getPinValue(mRightIndex))

        invalidate()
    }

    /**
     * Get marginLeft in each of the public attribute methods.
     *
     * @return float marginLeft
     */
    private fun getMarginLeft(): Float {
        return Math.max(mExpandedPinRadius, mCircleSize)
    }

    /**
     * Get yPos in each of the public attribute methods.
     *
     * @return float yPos
     */
    private fun getYPos(): Float {
        return (getHeight() - mBarPaddingBottom)
    }

    /**
     * Get barLength in each of the public attribute methods.
     *
     * @return float barLength
     */
    private fun getBarLength(): Float {
        return (getWidth() - 2 * getMarginLeft())
    }

    /**
     * Returns if either index is outside the range of the tickCount.
     *
     * @param leftThumbIndex  Integer specifying the left thumb index.
     * @param rightThumbIndex Integer specifying the right thumb index.
     * @return boolean If the index is out of range.
     */
    private fun indexOutOfRange(leftThumbIndex: Int, rightThumbIndex: Int): Boolean {
        return ((leftThumbIndex < 0 || leftThumbIndex >= mTickCount
                || rightThumbIndex < 0
                || rightThumbIndex >= mTickCount))
    }

    /**
     * Returns if either value is outside the range of the tickCount.
     *
     * @param leftThumbValue  Float specifying the left thumb value.
     * @param rightThumbValue Float specifying the right thumb value.
     * @return boolean If the index is out of range.
     */
    private fun valueOutOfRange(leftThumbValue: Float, rightThumbValue: Float): Boolean {
        return ((leftThumbValue < mTickStart || leftThumbValue > mTickEnd
                || rightThumbValue < mTickStart || rightThumbValue > mTickEnd))
    }

    /**
     * If is invalid tickCount, rejects. TickCount must be greater than 1
     *
     * @param tickCount Integer
     * @return boolean: whether tickCount > 1
     */
    private fun isValidTickCount(tickCount: Int): Boolean {
        return (tickCount > 1)
    }

    /**
     * Gets the distance between x and the left pin. If the left and right pins are equal, this
     * returns 0 if x is < the pins' position. Also returns 0 if the bar is not a range bar.
     *
     * @param x the x-coordinate to be checked
     * @return the distance between x and the left pin, or 0 if the pins are equal and x is to the left.
     * Also returns 0 if the bar is not a range bar.
     */
    private fun getLeftThumbXDistance(x: Float): Float {
        if (isRangeBar()) {
            val leftThumbX = mLeftThumb.x
            return if ((leftThumbX == mRightThumb.x && x < leftThumbX)) 0F else Math.abs(leftThumbX - x)
        } else {
            return 0f
        }
    }

    /**
     * Gets the distance between x and the right pin
     *
     * @param x the x-coordinate to be checked
     * @return the distance between x and the right pin
     */
    private fun getRightThumbXDistance(x: Float): Float {
        return Math.abs(mRightThumb.x - x)
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_DOWN] event.
     *
     * @param x the x-coordinate of the down action
     * @param y the y-coordinate of the down action
     */
    private fun onActionDown(x: Float, y: Float) {
        if (mIsRangeBar) {
            if (!mRightThumb!!.isPressed && mLeftThumb.isInTargetZone(x, y)) {

                pressPin(mLeftThumb)

            } else if (!mLeftThumb!!.isPressed && mRightThumb!!.isInTargetZone(x, y)) {

                pressPin(mRightThumb)
            }
        } else {
            if (mRightThumb!!.isInTargetZone(x, y)) {
                pressPin(mRightThumb)
            }
        }
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_UP] or
     * [android.view.MotionEvent.ACTION_CANCEL] event.
     *
     * @param x the x-coordinate of the up action
     * @param y the y-coordinate of the up action
     */
    private fun onActionUp(x: Float, y: Float) {
        if (mIsRangeBar && mLeftThumb!!.isPressed) {

            releasePin(mLeftThumb)

        } else if (mRightThumb!!.isPressed) {

            releasePin(mRightThumb)

        } else {
            val leftThumbXDistance = getLeftThumbXDistance(x)
            val rightThumbXDistance = getRightThumbXDistance(x)
            //move if is rangeBar and left index is lower of right one
            //if is not range bar leftThumbXDistance is always 0
            if (leftThumbXDistance < rightThumbXDistance && mIsRangeBar) {
                mLeftThumb!!.x = x
                releasePin(mLeftThumb)
            } else {
                mRightThumb!!.x = x
                releasePin(mRightThumb)
            }

            // Get the updated nearest tick marks for each thumb.
            val newLeftIndex = if (mIsRangeBar) mBar!!.getNearestTickIndex(mLeftThumb) else 0
            val newRightIndex = mBar!!.getNearestTickIndex(mRightThumb)
            // If either of the indices have changed, update and call the listener.
            if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {

                mLeftIndex = newLeftIndex
                mRightIndex = newRightIndex

                if (mListener != null) {
                    mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                            getPinValue(mLeftIndex),
                            getPinValue(mRightIndex))
                }
            }
        }
    }

    /**
     * Handles a [android.view.MotionEvent.ACTION_MOVE] event.
     *
     * @param x the x-coordinate of the move event
     */
    private fun onActionMove(x: Float) {

        // Move the pressed thumb to the new x-position.
        if (mIsRangeBar && mLeftThumb!!.isPressed) {
            movePin(mLeftThumb, x)
        } else if (mRightThumb!!.isPressed) {
            movePin(mRightThumb, x)
        }

        // If the thumbs have switched order, fix the references.
        if (mIsRangeBar && mLeftThumb!!.x > mRightThumb!!.x) {
            val temp = mLeftThumb
            mLeftThumb = mRightThumb
            mRightThumb = temp
        }

        // Get the updated nearest tick marks for each thumb.
        var newLeftIndex = if (mIsRangeBar) mBar!!.getNearestTickIndex(mLeftThumb) else 0
        var newRightIndex = mBar!!.getNearestTickIndex(mRightThumb)

        val componentLeft = getPaddingLeft()
        val componentRight = getRight() - getPaddingRight() - componentLeft

        if (x <= componentLeft) {
            newLeftIndex = 0
            movePin(mLeftThumb, mBar!!.getLeftX())
        } else if (x >= componentRight) {
            newRightIndex = getTickCount() - 1
            movePin(mRightThumb, mBar!!.getRightX())
        }
        /// end added code
        // If either of the indices have changed, update and call the listener.
        if (newLeftIndex != mLeftIndex || newRightIndex != mRightIndex) {

            mLeftIndex = newLeftIndex
            mRightIndex = newRightIndex
            if (mIsRangeBar) {
                mLeftThumb!!.setXValue(getPinValue(mLeftIndex))
            }
            mRightThumb!!.setXValue(getPinValue(mRightIndex))

            if (mListener != null) {
                mListener!!.onRangeChangeListener(this, mLeftIndex, mRightIndex,
                        getPinValue(mLeftIndex),
                        getPinValue(mRightIndex))
            }
        }
    }

    /**
     * Set the thumb to be in the pressed state and calls invalidate() to redraw
     * the canvas to reflect the updated state.
     *
     * @param thumb the thumb to press
     */
    private fun pressPin(thumb: PinView?) {
        if (mFirstSetTickCount) {
            mFirstSetTickCount = false
        }
        if (mArePinsTemporary) {
            val animator = ValueAnimator.ofFloat(0F, mExpandedPinRadius)
            animator.addUpdateListener { animation ->
                mThumbRadiusDP = (animation.animatedValue) as Float
                thumb!!.setSize(mThumbRadiusDP, mPinPadding * animation.animatedFraction)
                invalidate()
            }
            animator.start()
        }

        thumb!!.press()
    }

    /**
     * Set the thumb to be in the normal/un-pressed state and calls invalidate()
     * to redraw the canvas to reflect the updated state.
     *
     * @param thumb the thumb to release
     */
    private fun releasePin(thumb: PinView?) {

        val nearestTickX = mBar!!.getNearestTickCoordinate(thumb)
        thumb!!.x = nearestTickX
        val tickIndex = mBar!!.getNearestTickIndex(thumb)
        thumb.setXValue(getPinValue(tickIndex))

        if (mArePinsTemporary) {
            val animator = ValueAnimator.ofFloat(mExpandedPinRadius, 0F)
            animator.addUpdateListener { animation ->
                mThumbRadiusDP = (animation.animatedValue) as Float
                thumb.setSize(mThumbRadiusDP,
                        mPinPadding - (mPinPadding * animation.animatedFraction))
                invalidate()
            }
            animator.start()
        } else {
            invalidate()
        }

        thumb.release()
    }

    /**
     * Set the value on the thumb pin, either from map or calculated from the tick intervals
     * Integer check to format decimals as whole numbers
     *
     * @param tickIndex the index to set the value for
     */
    private fun getPinValue(tickIndex: Int): String {
        if (mPinTextListener != null) {
            return mPinTextListener!!.getPinValue(this, tickIndex)
        }
        val tickValue = if ((tickIndex == (mTickCount - 1)))
            mTickEnd
        else
            (tickIndex * mTickInterval) + mTickStart
        var xValue: String? = mTickMap!![tickValue]
        if (xValue == null) {
            if (tickValue.toDouble() == Math.ceil(tickValue.toDouble())) {
                xValue = (tickValue.toInt()).toString()
            } else {
                xValue = (tickValue).toString()
            }
        }
        return mPinTextFormatter.getText(xValue)
    }

    /**
     * Moves the thumb to the given x-coordinate.
     *
     * @param thumb the thumb to move
     * @param x     the x-coordinate to move the thumb to
     */
    private fun movePin(thumb: PinView?, x: Float) {

        // If the user has moved their finger outside the range of the bar,
        // do not move the thumbs past the edge.
        if (x < mBar!!.getLeftX() || x > mBar!!.getRightX()) {
            // Do nothing.
        } else if (thumb != null) {
            thumb.x = x
            invalidate()
        }
    }

    // Inner Classes ///////////////////////////////////////////////////////////

    /**
     * A callback that notifies clients when the RangeBar has changed. The
     * listener will only be called when either thumb's index has changed - not
     * for every movement of the thumb.
     */
    interface OnRangeBarChangeListener {

        fun onRangeChangeListener(rangeBar: RangeBar, leftPinIndex: Int,
                                  rightPinIndex: Int, leftPinValue: String, rightPinValue: String)
    }

    interface PinTextFormatter {

        fun getText(value: String): String
    }

    /**
     * @author robmunro
     * A callback that allows getting pin text exernally
     */
    interface OnRangeBarTextListener {

        fun getPinValue(rangeBar: RangeBar, tickIndex: Int): String
    }


}

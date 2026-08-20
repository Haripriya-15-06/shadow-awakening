package com.daki.shadowawakening

import android.content.Context
import android.graphics.*
import android.media.SoundPool
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import kotlin.math.*
import kotlin.random.Random

class MainActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        setContentView(ShadowView(this))
    }
}

private class ShadowView(context: Context) : View(context) {
    private val portrait = BitmapFactory.decodeResource(resources, R.drawable.warrior_portrait)
    private val full = BitmapFactory.decodeResource(resources, R.drawable.warrior_full)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        letterSpacing = 0.18f
    }

    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val thump = soundPool.load(context, R.raw.impact_thump, 1)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var start = 0L
    private var running = false
    private var impactDone = false
    private var touchX = 0f
    private var touchY = 0f
    private val particles = ArrayList<Particle>()

    private val cyan = Color.rgb(91, 246, 255)

    init {
        setBackgroundColor(Color.BLACK)
        isFocusable = true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP && !running) {
            touchX = e.x
            touchY = e.y
            running = true
            impactDone = false
            start = System.currentTimeMillis()
            particles.clear()
            invalidate()
            return true
        }
        return true
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val now = System.currentTimeMillis()
        val t = if (running) (now - start) / 1000f else 0f

        c.drawColor(Color.BLACK)
        val shake = if (t in 2.0f..2.5f) ((2.5f - t) / 0.5f) * 14f else 0f
        c.save()
        if (shake > 0) c.translate(
            Random.nextFloat() * shake - shake/2,
            Random.nextFloat() * shake - shake/2
        )

        when {
            !running -> drawIdle(c)
            t < 0.38f -> drawActivation(c, t)
            t < 1.15f -> drawZoomOut(c, t)
            t < 1.75f -> drawFlight(c, t)
            t < 2.55f -> drawLanding(c, t)
            else -> {
                drawFinal(c, t)
                running = false
            }
        }
        c.restore()

        if (running) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawIdle(c: Canvas) {
        drawCenteredBitmap(c, portrait, width/2f, height*0.38f, min(width,height)*0.62f)
        drawHudRing(c, width/2f, height*0.38f, min(width,height)*0.30f, 0.22f)
        text.color = Color.argb(170, 91, 246, 255)
        text.textSize = 28f
        text.textAlign = Paint.Align.CENTER
        c.drawText("TOUCH TO AWAKEN", width/2f, height*0.78f, text)
        text.textSize = 14f
        text.color = Color.argb(100, 190, 250, 255)
        c.drawText("SHADOW SYSTEM  //  STANDBY", width/2f, height*0.82f, text)
    }

    private fun drawActivation(c: Canvas, t: Float) {
        drawCenteredBitmap(c, portrait, width/2f, height*0.38f, min(width,height)*0.62f)
        val p = easeOut(min(1f, t/0.38f))
        val cx = width/2f
        val cy = height*0.47f
        for (i in 0..4) {
            drawHudRing(c, cx, cy, min(width,height)*(0.055f + i*0.032f)*p, 0.7f - i*0.09f)
        }
        glow.color = Color.argb((230*p).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 5f + 18f*p
        c.drawCircle(cx, cy, min(width,height)*0.065f*p, glow)
        glow.style = Paint.Style.FILL
        glow.maskFilter = BlurMaskFilter(28f*p, BlurMaskFilter.Blur.NORMAL)
        c.drawCircle(cx, cy, 18f + 22f*p, glow)
        glow.maskFilter = null

        // Eyes + headband/ribbon activation
        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 5f
        glow.color = Color.argb(230, Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        c.drawLine(cx-58, height*0.285f, cx-25, height*0.28f, glow)
        c.drawLine(cx+25, height*0.28f, cx+58, height*0.285f, glow)
        glow.style = Paint.Style.FILL

        drawRunes(c, p, 0.45f)
        text.color = Color.argb((210*p).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        text.textSize = 15f
        text.textAlign = Paint.Align.CENTER
        c.drawText("CORE LINK  //  03%", cx, height*0.86f, text)
    }

    private fun drawZoomOut(c: Canvas, t: Float) {
        val p = easeInOut((t-0.38f)/0.77f)
        // Portrait fades while full-body asset scales in.
        drawCenteredBitmap(c, portrait, width/2f, height*(0.38f - 0.05f*p), min(width,height)*(0.62f + 0.08f*p), 1f-p)
        drawCenteredBitmap(c, full, width/2f, height*(0.50f - 0.03f*p), min(width,height)*(0.95f + 0.10f*p), 0.2f + 0.8f*p)
        drawRunes(c, 0.6f + 0.4f*p, 0.9f)
        drawTechLines(c, p)
    }

    private fun drawFlight(c: Canvas, t: Float) {
        val p = easeInOut((t-1.15f)/0.60f)
        val y = height*0.55f - height*0.33f*p
        drawCenteredBitmap(c, full, width/2f, y, min(width,height)*1.00f, 1f)
        drawRunes(c, 1f, 1f)
        // Energy trail
        glow.color = Color.argb(120, Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 3f
        for (i in 0..8) {
            val yy = y + height*0.18f + i*22f
            c.drawLine(width*0.5f - i*7, yy, width*0.5f + i*7, yy+35f, glow)
        }
        glow.style = Paint.Style.FILL
        text.color = Color.argb(170, Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        text.textSize = 13f
        text.textAlign = Paint.Align.CENTER
        c.drawText("VECTOR THRUST // ONLINE", width/2f, height*0.92f, text)
    }

    private fun drawLanding(c: Canvas, t: Float) {
        val p = (t-1.75f)/0.80f
        val y: Float
        val scale: Float
        if (p < 0.60f) {
            val q = easeInOut(p/0.60f)
            y = height*0.22f + height*0.43f*q
            scale = 0.95f + 0.10f*q
        } else {
            val q = easeOut((p-0.60f)/0.40f)
            y = height*0.65f + height*0.02f*q
            scale = 1.05f - 0.04f*q
        }
        drawCenteredBitmap(c, full, width/2f, y, min(width,height)*scale, 1f)
        drawRunes(c, 1f, 1f)

        val impact = if (p > 0.60f) easeOut((p-0.60f)/0.40f) else 0f
        if (p > 0.60f) {
            glow.color = Color.argb((180*(1-impact)).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
            glow.style = Paint.Style.STROKE
            glow.strokeWidth = 5f
            c.drawCircle(width/2f, y+height*0.24f, height*(0.05f+0.20f*impact), glow)
            glow.strokeWidth = 2f
            c.drawCircle(width/2f, y+height*0.24f, height*(0.10f+0.32f*impact), glow)
            glow.style = Paint.Style.FILL
            if (!impactDone) {
                impactDone = true
                soundPool.play(thump, 1f, 1f, 1, 0, 1f)
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        }
    }

    private fun drawFinal(c: Canvas, t: Float) {
        drawCenteredBitmap(c, full, width/2f, height*0.55f, min(width,height)*0.98f, 1f)
        drawRunes(c, 1f, 1f)
        drawTechLines(c, 1f)
        text.color = Color.argb(210, Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        text.textAlign = Paint.Align.CENTER
        text.textSize = 18f
        c.drawText("AWAKENED", width/2f, height*0.91f, text)
    }

    private fun drawCenteredBitmap(c: Canvas, b: Bitmap, cx: Float, cy: Float, size: Float, alpha: Float = 1f) {
        val scale = size / max(b.width, b.height).toFloat()
        val dw = b.width*scale
        val dh = b.height*scale
        val r = RectF(cx-dw/2, cy-dh/2, cx+dw/2, cy+dh/2)
        paint.alpha = (255*alpha).toInt().coerceIn(0,255)
        c.drawBitmap(b, null, r, paint)
        paint.alpha = 255
    }

    private fun drawHudRing(c: Canvas, x: Float, y: Float, r: Float, a: Float) {
        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 2f
        glow.color = Color.argb((255*a).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        c.drawCircle(x,y,r,glow)
        c.drawArc(RectF(x-r*1.2f,y-r*1.2f,x+r*1.2f,y+r*1.2f),-50f,100f,false,glow)
        c.drawArc(RectF(x-r*1.35f,y-r*1.35f,x+r*1.35f,y+r*1.35f),130f,100f,false,glow)
    }

    private fun drawRunes(c: Canvas, p: Float, alpha: Float) {
        text.textAlign = Paint.Align.CENTER
        text.textSize = 20f
        text.color = Color.argb((150*p*alpha).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        val glyphs = listOf("影","刃","戦","魂","力","迅","守","夜","零","天")
        for (i in glyphs.indices) {
            val x = width*(0.19f + (i%3)*0.31f)
            val y = height*(0.30f + (i/3)*0.12f)
            c.drawText(glyphs[i], x, y, text)
        }
    }

    private fun drawTechLines(c: Canvas, p: Float) {
        glow.style = Paint.Style.STROKE
        glow.strokeWidth = 1f
        glow.color = Color.argb((110*p).toInt(), Color.red(cyan), Color.green(cyan), Color.blue(cyan))
        for (i in 0..5) {
            val yy = height*(0.12f+i*0.14f)
            c.drawLine(24f, yy, 105f + 60f*p, yy, glow)
            c.drawLine(width-105f-60f*p, yy, width-24f, yy, glow)
        }
        glow.style = Paint.Style.FILL
    }

    private fun easeInOut(x: Float): Float {
        val v = x.coerceIn(0f,1f)
        return if (v < .5f) 2*v*v else 1f - (-2*v+2).pow(2)/2f
    }
    private fun easeOut(x: Float): Float {
        val v=x.coerceIn(0f,1f)
        return 1f-(1f-v)*(1f-v)*(1f-v)
    }

    private data class Particle(val x:Float,val y:Float)
}

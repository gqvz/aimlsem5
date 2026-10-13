package org.sahay.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

/** Locally generated ambient audio; no asset download or provider is needed. */
object AmbientAudio {
    @Volatile var volume = .3f
    @Volatile private var playing = false
    private var worker: Thread? = null
    @Synchronized fun play(kind: String, value: Float) {
        stop(); volume=value; playing=true
        worker=Thread {
            val track=AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(22050).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(8192).setTransferMode(AudioTrack.MODE_STREAM).build()
            var brown=0f; var rain=0f
            try {
                track.play()
                val block=ShortArray(2048)
                while(playing && !Thread.currentThread().isInterrupted) {
                    for(i in block.indices) {
                        val random=Random.nextFloat()*2-1
                        brown=(brown+random*.025f).coerceIn(-.6f,.6f)*.995f
                        rain=rain*.6f+random*.4f
                        val sample=when(kind) { "brown" -> brown; "rain" -> rain; else -> random }
                        block[i]=(sample*volume*6500).toInt().toShort()
                    }
                    track.write(block,0,block.size)
                }
            } finally { runCatching { track.stop() }; track.release() }
        }.apply { name="sahay-ambient"; start() }
    }
    @Synchronized fun stop() {
        playing=false; worker?.interrupt(); worker?.join(500); worker=null
    }
}

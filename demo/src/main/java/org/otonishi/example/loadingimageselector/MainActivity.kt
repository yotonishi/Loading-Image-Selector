package org.otonishi.example.loadingimageselector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.otonishi.example.loadingimageselector.databinding.ActivityMainBinding
import org.otonishi.loadimageselector.LoadingImageSelector

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(
            this, R.layout.activity_main
        )
    }

    private var isFavorite = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.imageSelector.apply {
            setSelectorSelected(isFavorite)
            setDelayShowMs(2000L)
            show()
            setCallback(object : LoadingImageSelector.LoadingImageSelectorInterface {
                override fun startLoad(atEnd: () -> Unit) {
                    switchFavorite(atEnd)
                }
            })
        }
    }

    private fun switchFavorite(atEnd: () -> Unit) {
        isFavorite = !isFavorite
        atEnd()
    }
}

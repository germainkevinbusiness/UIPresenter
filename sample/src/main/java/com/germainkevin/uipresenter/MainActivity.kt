package com.germainkevin.uipresenter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import com.germainkevin.library.PresenterShadowLayer
import com.germainkevin.library.Presenter
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototype_impl.RotationYByAnimation
import com.germainkevin.uipresenter.databinding.ActivityMainBinding


/**
 * An example of the [com.germainkevin.library.UIPresenter] when called from a [android.app.Activity]
 *
 * This activity displays [MainFragment] inside its [androidx.fragment.app.FragmentContainerView]
 * */
class MainActivity : AppCompatActivity() {
    private val purple700 by lazy { ContextCompat.getColor(this, R.color.purple_700) }
    private val blue500 by lazy { ContextCompat.getColor(this, R.color.blue_500) }
    private val whiteColor by lazy { ContextCompat.getColor(this, R.color.white) }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Triggered when pressing on the play menu item on the toolbar
     * */
    private fun presentMenuItem() {
        UIPresenter(this).set(
            viewToPresentId = R.id.action_present_view,
            backgroundColor = purple700,
            descriptionText = getString(R.string.menu_play_desc),
            descriptionTextColor = whiteColor,
            revealAnimation = RotationYByAnimation(),
            presenterHasShadowedWindow = true,
            removePresenterOnAnyClickEvent = false,
            shadowLayer = PresenterShadowLayer(shadowColor = blue500),
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_present_view -> {
                presentMenuItem()
                true
            }
            else -> false
        }
    }
}
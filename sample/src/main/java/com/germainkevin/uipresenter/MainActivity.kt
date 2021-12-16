package com.germainkevin.uipresenter

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.germainkevin.library.PresenterShadowLayer
import com.germainkevin.library.presenter_view.Presenter
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototype_impl.NoRevealAnimation
import com.germainkevin.library.prototype_impl.RotationXByAnimation
import com.germainkevin.library.prototype_impl.RotationYByAnimation
import com.germainkevin.uipresenter.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val purple700 by lazy { ContextCompat.getColor(this, R.color.purple_700) }
    private val purple200 by lazy { ContextCompat.getColor(this, R.color.purple_200) }
    private val blue500 by lazy { ContextCompat.getColor(this, R.color.blue_500) }
    private val teal200 by lazy { ContextCompat.getColor(this, R.color.teal_200) }
    private val teal700 by lazy { ContextCompat.getColor(this, R.color.teal_700) }
    private val whiteColor by lazy { ContextCompat.getColor(this, R.color.white) }

    private lateinit var animals: MutableList<String>

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeRv()
    }

    private fun initializeRv() {
        animals = ArrayList()
        animals.addAll(listOf("Doggo", "Kitty", "Rabbit"))
        val animalRVAdapter = AnimalRVAdapter()
        animalRVAdapter.submitList(animals)
        binding.recyclerView.adapter = animalRVAdapter
    }

    private fun presentTextView() {
        UIPresenter(this).set(
            viewToPresent = binding.firstTextView,
            backgroundColor = blue500,
            descriptionText = "This is a text explaining what the below list is",
            descriptionTextColor = Color.BLACK,
            presenterHasShadowedWindow = true,
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentAnimalImage()
                }
            }
        )
    }

    private fun presentAnimalImage() {
        val vHParentView =
            binding.recyclerView.layoutManager?.findViewByPosition(0) as ConstraintLayout
        val viewHolder =
            binding.recyclerView.getChildViewHolder(vHParentView) as AnimalRVAdapter.AnimalViewHolder
        UIPresenter(this).set(
            viewToPresent = viewHolder.itemView.findViewById<ImageView>(R.id.animalImage),
            backgroundColor = blue500,
            descriptionText = "This is the image of the animal, shown inside this RecyclerView",
            descriptionTextColor = Color.BLACK,
            presenterHasShadowedWindow = true,
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentAnimalName()
                }
            }
        )
    }

    private fun presentAnimalName() {
        val vHParentView =
            binding.recyclerView.layoutManager?.findViewByPosition(0) as ConstraintLayout
        val viewHolder =
            binding.recyclerView.getChildViewHolder(vHParentView) as AnimalRVAdapter.AnimalViewHolder
        UIPresenter(this).set(
            viewToPresent = viewHolder.itemView.findViewById<TextView>(R.id.animalName),
            backgroundColor = blue500,
            descriptionText = "This is the name of the animal, shown inside this RecyclerView",
            descriptionTextColor = Color.BLACK,
            presenterHasShadowedWindow = true,
            revealAnimation = RotationYByAnimation(),
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentAnimalRow()
                }
            }
        )
    }

    private fun presentAnimalRow() {
        UIPresenter(this).set(
            viewToPresent = binding.recyclerView[2],
            backgroundColor = blue500,
            descriptionText = "This is a row inside the RecyclerView with animal image and name",
            descriptionTextColor = Color.BLACK,
            revealAnimation = RotationXByAnimation(),
            presenterHasShadowedWindow = true,
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentEditText()
                }
            }
        )
    }

    private fun presentEditText() {
        val descText0 =
            "This is the EditText. Here you can write Animal names and add them to the RecyclerView"
        UIPresenter(this).set(
            viewToPresent = binding.addEditText,
            backgroundColor = teal200,
            descriptionTextColor = Color.BLACK,
            descriptionText = descText0,
            presenterHasShadowedWindow = true,
            shadowLayer = PresenterShadowLayer(dx = 8f, dy = 8f),
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentBtn1()
                }
            }
        )
    }

    private fun presentBtn1() {
        val descText0 =
            "This is the add button. You can use it to add as many animals as you want " +
                    "inside the RecyclerView. All you have to do to add the item is to click on that " +
                    "button"
        UIPresenter(this).set(
            viewToPresent = binding.fab1,
            backgroundColor = teal200,
            descriptionTextColor = Color.BLACK,
            descriptionText = descText0,
            presenterHasShadowedWindow = true,
            shadowLayer = PresenterShadowLayer(dx = 8f, dy = 8f),
            revealAnimation = NoRevealAnimation(),
            removePresenterOnAnyClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentMenuItem()
                }
            }
        )
    }

    private fun presentMenuItem() {
        val descText0 = "This is a play button, placed on a Toolbar"
        UIPresenter(this).set(
            viewToPresentId = R.id.action_present_view,
            backgroundColor = purple700,
            descriptionText = descText0,
            descriptionTextColor = whiteColor,
            revealAnimation = RotationYByAnimation(),
            presenterHasShadowedWindow = true,
            removePresenterOnAnyClickEvent = false,
            shadowLayer = PresenterShadowLayer(shadowColor = blue500),
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    Toast.makeText(this, "Done presenting UI!", Toast.LENGTH_SHORT).show()
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
                presentTextView()
                true
            }
            else -> false
        }
    }
}
package com.germainkevin.uipresenter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.germainkevin.library.Presenter
import com.germainkevin.library.ShadowLayer
import com.germainkevin.library.UIPresenter
import com.germainkevin.library.prototype_impl.*
import com.germainkevin.uipresenter.databinding.MainFragmentBinding

/**
 * An example of the [com.germainkevin.library.UIPresenter] when called from a [Fragment]
 *
 * To start displaying [UIPresenters][com.germainkevin.library.UIPresenter], click on the + (plus)
 * button on this fragment's UI
 * */
class MainFragment : Fragment() {
    private val blue500 by lazy { ContextCompat.getColor(requireContext(), R.color.blue_500) }
    private val teal200 by lazy { ContextCompat.getColor(requireContext(), R.color.teal_200) }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        initialize()
        return binding.root
    }

    private fun initialize() {
        AnimalRVAdapter().apply {
            submitList(listOf("Doggo", "Kitty", "Rabbit"))
            binding.recyclerView.adapter = this
        }
        binding.fab1.setOnClickListener { presentTextView() }
    }

    private fun presentTextView() {
        UIPresenter(this).set(
            viewToPresentId = R.id.firstTextView,
            backgroundColor = blue500,
            descriptionText = getString(R.string.textView_desc),
            descriptionTextColor = Color.BLACK,
            shadowedWindowEnabled = true,
            // Now the library won't removes the presenter on any detected click event automatically
            // You now have to decide which click event will remove the presenter by yourself, like
            // show inside the presenterStateChangeListener below
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                // Removes the presenter when a click is done on the
                // presenter's PresenterShape which is the presenter's visible part
                // with the description text, background & shadow layer
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
        val mViewToPresent = viewHolder.itemView.findViewById<ImageView>(R.id.animalImage)
        UIPresenter(this).set(
            viewToPresent = mViewToPresent,
            backgroundColor = blue500,
            descriptionText = getString(R.string.animal_image_desc),
            descriptionTextColor = Color.BLACK,
            shadowedWindowEnabled = true,
            revealAnimation = CircularReveal(),
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                // Removes the presenter when a click is detected on $mViewToPresent
                if (state == Presenter.STATE_VTP_PRESSED) {
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
        val mViewToPresent = viewHolder.itemView.findViewById<TextView>(R.id.animalName)
        UIPresenter(this).set(
            viewToPresent = mViewToPresent,
            backgroundColor = blue500,
            descriptionText = getString(R.string.animal_name_desc),
            descriptionTextColor = Color.BLACK,
            shadowedWindowEnabled = true,
            revealAnimation = HorizontalRotation(),
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                // Removes the presenter when a click is done outside the
                // $mViewToPresent bounds and outside the visible part of the presenter
                if (state == Presenter.STATE_NON_FOCAL_PRESSED) {
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
            descriptionText = getString(R.string.animal_row_desc),
            descriptionTextColor = Color.BLACK,
            revealAnimation = VerticalRotation(),
            shadowedWindowEnabled = true,
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentEditText()
                }
            }
        )
    }

    private fun presentEditText() {
        UIPresenter(this).set(
            viewToPresent = binding.addEditText,
            backgroundColor = teal200,
            descriptionText = getString(R.string.editText_desc),
            descriptionTextColor = Color.BLACK,
            shadowedWindowEnabled = true,
            revealAnimation = FadeInAndScale(),
            shadowLayer = ShadowLayer(shadowColor = blue500),
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    presentBtn1()
                }
            }
        )
    }

    private fun presentBtn1() {
        UIPresenter(this).set(
            viewToPresent = binding.fab1,
            backgroundColor = teal200,
            descriptionTextColor = Color.BLACK,
            descriptionText = getString(R.string.fab1_desc),
            shadowedWindowEnabled = true,
            revealAnimation = NoAnimation(),
            removeAfterAnyDetectedClickEvent = false,
            presenterStateChangeListener = { state, removePresenter ->
                if (state == Presenter.STATE_FOCAL_PRESSED) {
                    removePresenter()
                    Toast.makeText(requireContext(), "Done presenting UI!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }
}
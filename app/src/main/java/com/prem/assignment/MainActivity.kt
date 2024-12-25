package com.prem.assignment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.prem.assignment.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var xDelta = 0
    private var yDelta = 0

    private var layoutWidth = 0
    private var layoutHeight = 0
    private lateinit var binding: ActivityMainBinding


    private var undoStackCallback: UndoStackCallback? = null

    private var dialogAddTextBtn: Button? = null
    private lateinit var dialogEditText: EditText
    private var dialog: AlertDialog? = null
    private var textView: TextView? = null
    private var IS_UNDO: Boolean = false

    private val fontNames = listOf(
        "sans-serif",
        "serif",
        "monospace",
        "sans-serif-condensed",
        "sans-serif-medium",
        "cursive"
    )
    private val undoStack = ArrayList<TextViewAction>()
    private val redoStack = ArrayList<TextViewAction>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)


        undoStackCallback = object : UndoStackCallback {
            override fun onUndoStackUpdated() {
                // Handle the update here (e.g., update the UI or log the action)
                if (IS_UNDO) {
                    redoStack.clear()
                    IS_UNDO = false
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            it.background = AppCompatResources.getDrawable(this, R.drawable.ripple_icon_background)
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addBtn.setOnClickListener {

            dialog = AlertDialog.Builder(this)
                .setView(R.layout.add_text_dialog)
                .create()
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.show()

            dialogAddTextBtn = dialog!!.findViewById<Button>(R.id.add_text_btn)!!
            dialogEditText = dialog!!.findViewById<EditText>(R.id.text_edtv)!!

            // When a new TextView is added
            dialogAddTextBtn?.setOnClickListener {
                val text = dialogEditText.text.toString()
                if (text != "") {
                    dialog!!.hide()

                    // Create new TextView
                    val newTextView = TextView(this)
                    newTextView.text = text
                    newTextView.textSize = 25f

                    // Set initial layout parameters
                    val layoutParams = RelativeLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.leftMargin = 0
                    layoutParams.topMargin = 0
                    newTextView.layoutParams = layoutParams

                    // Add the TextView to the layout
                    binding.constraintRL.addView(newTextView)

                    // Track the action for undo/redo
                    val action = TextViewAction(
                        actionType = ActionType.ADD,
                        textView = newTextView,
                        oldText = newTextView.text.toString(),
                        oldTextSize = 25,
                        oldTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    )
                    undoStack.add(action)
                    undoStackCallback?.onUndoStackUpdated()
//                    Log.d("value inital add action", action.toString())
//                    Log.e("value inital add undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })

                    binding.subSizeBtn.isEnabled = true
                    binding.addSizeBtn.isEnabled = true
                    binding.boldBtn.isEnabled = true
                    binding.italicBtn.isEnabled = true
                    binding.fontSelectionBtn.isEnabled = true
                    binding.sizeViewTv.text = newTextView.textSize.toInt().div(3).toString()
                    this.textView = newTextView

                    // Set up the click listener for the TextView
                    newTextView.setOnClickListener {
                        this.textView = newTextView
                        binding.sizeViewTv.text = newTextView.textSize.toInt().div(3).toString()
                        Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show()
                    }
                    newTextView.setOnTouchListener(onTouchListener())
                } else {
                    Toast.makeText(this, "Enter text", Toast.LENGTH_SHORT).show()
                }
            }

        }

        binding.undoBtn.setOnClickListener {
//            Log.d("undobtn before Pressed",undoStack.joinToString {it.oldText + " : " + it.oldTextSize + " : ".plus(it.actionType).plus(",")})
            undo() // Call undo when button is pressed
//            Log.e("undobtn after Pressed",undoStack.joinToString {it.oldText + " : " + it.oldTextSize + " : ".plus(it.actionType).plus(",")})
        }

        binding.redoBtn.setOnClickListener {
//            Log.e("undoredobtn ",redoStack.joinToString {it.oldText + " : " + it.oldTextSize + " : ".plus(it.actionType).plus(",")})
            redo() // Call redo when button is pressed

        }



        binding.subSizeBtn.setOnClickListener {
            val oldSize = binding.sizeViewTv.text.toString().toInt()
            if (oldSize > 0) {
                undoStackCallback?.onUndoStackUpdated()

                textView!!.textSize = oldSize.minus(1).toFloat()
                binding.sizeViewTv.text = oldSize.minus(1).toString()

                val action =
                    getActionObj(
                        textView!!,
                        ActionType.UPDATE_TEXT_SIZE,
                        textView!!.typeface
                    )
//                Log.d("value sub btn", action.toString())
//                Log.e("value sub undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldTextSize })

                undoStack.add(action)
            }
        }


        binding.addSizeBtn.setOnClickListener {
            undoStackCallback?.onUndoStackUpdated()
            val oldSize = binding.sizeViewTv.text.toString().toInt()
            textView!!.textSize = oldSize.plus(1).toFloat()
            binding.sizeViewTv.text = oldSize.plus(1).toString()

            val action = getActionObj(
                textView!!,
                ActionType.UPDATE_TEXT_SIZE,
                textView!!.typeface
            )

            undoStack.add(action)
//            Log.d("value add action", action.toString())
//            Log.e("value add undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })

        }


        binding.boldBtn.setOnClickListener {
            val typeface = textView!!.typeface

            val newTypeface: Typeface = if (typeface.isItalic && typeface.isBold) {
                Typeface.create(typeface, Typeface.ITALIC)  // Keep italic, remove bold
            } else if (typeface.isItalic) {
                Typeface.create(typeface, Typeface.BOLD_ITALIC) // Set both bold and italic
            } else if (typeface.isBold) {
                Typeface.create(typeface, Typeface.NORMAL)  // Remove bold
            } else {
                Typeface.create(typeface, Typeface.BOLD)  // Set bold
            }

            undoStackCallback?.onUndoStackUpdated()
            textView!!.setTypeface(newTypeface)  // Apply new typeface
            // Save the action for undo
            val action = getActionObj(
                textView!!,
                ActionType.UPDATE_TEXT_STYLE_IS_BOLD,
                textView!!.typeface
            )
            undoStack.add(action)
//            Log.d("value bold btn", action.toString())
//            Log.e("value bold btn undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + " : " + it.oldText })

        }


        binding.italicBtn.setOnClickListener {
            val typeface = textView!!.typeface

            val newTypeface: Typeface = if (typeface.isItalic && typeface.isBold) {
                Typeface.create(typeface, Typeface.BOLD) // Remove italic, keep bold
            } else if (typeface.isItalic) {
                Typeface.create(typeface, Typeface.NORMAL) // Remove italic
            } else if (typeface.isBold) {
                Typeface.create(typeface, Typeface.BOLD_ITALIC) // Add italic
            } else {
                Typeface.create(typeface, Typeface.ITALIC) // Set italic
            }

            undoStackCallback?.onUndoStackUpdated()
            textView!!.setTypeface(newTypeface)  // Apply new typeface

            // Save the action for undo
            val action = getActionObj(
                textView!!,
                ActionType.UPDATE_TEXT_STYLE_ITALIC,
                textView!!.typeface
            )

            undoStack.add(action)
//            Log.d("value italic action", action.toString())
//            Log.e("value italic undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })

        }


        binding.fontSelectionBtn.setOnClickListener {
            // Create the dialog builder
            val builder = AlertDialog.Builder(this)

            // Create a ListView to show the list of fonts
            val fontListView = ListView(this)
            fontListView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                fontNames
            )

            builder.setTitle("Select Font")
            builder.setView(fontListView)
            val dialog = builder.create()
            dialog.show()

            // Handle item clicks
            fontListView.setOnItemClickListener { _, _, position, _ ->
                val selectedFont = fontNames[position]
                val currentTypeface = textView?.typeface

//                undoStack.add(getActionObj(textView!!, ActionType.UPDATE_TEXT_STYLE_IS_BOLD, textView!!.typeface))
                undoStackCallback?.onUndoStackUpdated()
                textView?.typeface = Typeface.create(selectedFont, Typeface.NORMAL)
                // Save the action for undo
                val action = getActionObj(
                    textView!!,
                    ActionType.UPDATE_FONT,
                    textView!!.typeface
                )

                undoStack.add(action)


//                Log.e("value undo font font",   undoStack.joinToString { it.oldText+" "+it.actionType })
//                Log.d("value  font action", action.toString())
//                Log.e("value font undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })


                dialog.dismiss()
            }
        }

        // Ensure we have layout size after it's been measured
        binding.constraintRL.post {
            layoutWidth = binding.constraintRL.width
            layoutHeight = binding.constraintRL.height
        }
    }

    @SuppressLint("ClickableViewAccessibility")    // handle the textview move logic
    private fun onTouchListener(): OnTouchListener {
        return OnTouchListener { view, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val lParams = view.layoutParams as RelativeLayout.LayoutParams

                    // Get the initial touch offsets (where the user touched the image)
                    xDelta = x - lParams.leftMargin
                    yDelta = y - lParams.topMargin
                }

                MotionEvent.ACTION_UP -> {
                    // debug log when the user stops touching
//                    Log.d("Motion", "Action UP")
                }

                MotionEvent.ACTION_MOVE -> {
                    val layoutParams = view.layoutParams as RelativeLayout.LayoutParams

                    // Calculate the new left and top margins
                    var newLeftMargin = x - xDelta
                    var newTopMargin = y - yDelta

                    // image doesn't move outside the bounds of the layout
                    newLeftMargin = newLeftMargin.coerceIn(0, layoutWidth - view.width)
                    newTopMargin = newTopMargin.coerceIn(0, layoutHeight - view.height)

                    // Set calculated margins
                    layoutParams.leftMargin = newLeftMargin
                    layoutParams.topMargin = newTopMargin

                    // Make sure the image doesn't overlap the bottom or right edges
                    layoutParams.rightMargin = 0
                    layoutParams.bottomMargin = 0

                    // Apply the new layout parameters
                    view.layoutParams = layoutParams
                }
            }
            // Invalidate the layout to trigger a redraw
            binding.constraintRL.invalidate()
            false
        }
    }

    private fun getActionObj(
        textView: TextView,
        actionType: ActionType,
        typeface: Typeface
    ): TextViewAction {
        return TextViewAction(
            actionType = actionType,
            textView = textView,
            oldText = textView.text.toString(),
            oldTextSize = textView.textSize.div(3).toInt(),
            oldTypeface = textView.typeface
        )
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            IS_UNDO = true
            var action = undoStack.removeAt(undoStack.size - 1) // Pop last action
            // Push to redo stack
            redoStack.add(action)

            if (undoStack.isNotEmpty()) {
                action = undoStack[undoStack.size - 1]
            }

//            Log.d("value undo action", action.toString())
//            Log.e("value undo undoList",undoStack.size.toString() + " : " + undoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })
//            Log.e("value redo undoList",redoStack.size.toString() + " : " + redoStack.joinToString { it.actionType.toString() + " : " + it.oldTextSize + " : " + it.oldText })

            when (action.actionType) {
                ActionType.ADD -> {
                    action.textView.text = action.oldText
                    action.textView.typeface = action.oldTypeface
                    action.textView.textSize = action.oldTextSize.toFloat()
                    action.actionType = ActionType.REMOVE // Remove the added TextView
                    binding.sizeViewTv.text =
                        if (action.oldTextSize.toString() != "") action.oldTextSize.toString() else "0"
                    undoStack.add(action)
                    return
                }
                ActionType.REMOVE -> {
                    binding.constraintRL.removeView(action.textView) // Add the removed TextView back
                    binding.sizeViewTv.text =
                        if (action.oldTextSize.toString() != "") action.oldTextSize.toString() else "0"
                }

                ActionType.UPDATE_TEXT_SIZE -> {
                    action.textView.textSize = action.oldTextSize.toFloat() // Revert text size
                    binding.sizeViewTv.text = action.oldTextSize.toString()
                    action.textView.typeface = action.oldTypeface
                }

                ActionType.UPDATE_TEXT -> {
                    action.textView.text = action.oldText // Revert text
                    action.textView.typeface = action.oldTypeface
                }

                ActionType.UPDATE_FONT -> {
                    Log.e("value undo font",   action.oldText)
                    action.textView.typeface = action.oldTypeface // Revert font
                }

                ActionType.UPDATE_TEXT_STYLE_IS_BOLD -> {
                    action.textView.typeface = action.oldTypeface
                }

                ActionType.UPDATE_TEXT_STYLE_ITALIC -> {
                    action.textView.typeface = action.oldTypeface
                }

            }
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val action = redoStack.removeAt(redoStack.size - 1) // Pop last action

//            Log.d("value redobtn",redoStack.joinToString {it.oldText + " : " + it.oldTextSize + " : ".plus(it.actionType).plus(",")  })
//            Log.e("value redobtn action",action.oldText + " : " + action.oldTextSize + " : " + action.actionType)



            when (action.actionType) {
                ActionType.ADD -> {
//                    binding.constraintRL.removeView(action.textView) // Remove it again
                }

                ActionType.REMOVE -> {
                    action.textView.typeface = action.oldTypeface
                    action.textView.textSize = action.oldTextSize.toFloat()
                    action.actionType = ActionType.ADD
                    binding.constraintRL.addView(action.textView) // Add the TextView back
                    binding.sizeViewTv.text = action.oldTextSize.toString()
                }

                ActionType.UPDATE_TEXT_SIZE -> {
                    action.textView.textSize =
                        action.oldTextSize.toFloat() // Revert text size again
                    binding.sizeViewTv.text = action.oldTextSize.toString()
                }

                ActionType.UPDATE_TEXT -> {
                    action.textView.text = action.oldText // Revert text again
                }

                ActionType.UPDATE_FONT -> {
                    Log.e("value undo font",   undoStack.joinToString { it.oldText+" "+it.actionType })
                    Log.e("value redo font",   redoStack.joinToString { it.oldText+" "+it.actionType })
                    action.textView.typeface = action.oldTypeface // Revert font again
                }

                ActionType.UPDATE_TEXT_STYLE_IS_BOLD -> {
                    action.textView.typeface = action.oldTypeface // Revert font again
                }

                ActionType.UPDATE_TEXT_STYLE_ITALIC -> {
                    action.textView.typeface = action.oldTypeface // Revert font again
                }
            }
            // Push to undo stack
            undoStack.add(action)
        }
    }
}

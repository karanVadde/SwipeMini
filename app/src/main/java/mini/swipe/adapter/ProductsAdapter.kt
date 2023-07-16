package mini.swipe.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import mini.swipe.R
import mini.swipe.model.SwipeDataItem
import java.math.BigDecimal
import java.math.RoundingMode

class ProductsAdapter(swipeDataItemList : ArrayList<SwipeDataItem>)
        : RecyclerView.Adapter<ProductsAdapter.ViewHolder?>() {

    private val TAG = ProductsAdapter::class.java.name

    private var swipeDataItemList : ArrayList<SwipeDataItem>

    init {
        this.swipeDataItemList = swipeDataItemList
        Log.d(TAG, "size: ${swipeDataItemList.size}")
    }

    /**
     * use this function to update data items list.
     */
    fun updateDataItems(newItems : ArrayList<SwipeDataItem>){
        this.swipeDataItemList = newItems
    }

    /**
     * this field is used as a click listener for all the views in item_swipe item to
     * inform the user what a specific field is
     */
    object clickListener : View.OnClickListener{
        override fun onClick(p0: View?) {
            Log.d("onClick", "${p0?.id}")
            var stringToShow = ""
            val cxt = p0?.context
            cxt?.let {
                when(p0.id){
                    //based on view ids, we decide what to show the user.
                    R.id.prd_nme -> stringToShow = cxt.getString(R.string.prd_nme)
                    R.id.prd_type -> stringToShow = cxt.getString(R.string.prd_typ)
                    R.id.prd_prc -> stringToShow = cxt.getString(R.string.prd_prc)
                    R.id.prd_tax -> stringToShow = cxt.getString(R.string.prd_tax)
                }
                Toast.makeText(cxt, stringToShow, Toast.LENGTH_SHORT).show()
            }

        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var prodImageView: ImageView
        var prodName : TextView
        var prodType : TextView
        var prodPrice : TextView
        var prodTax : TextView
        init {
            prodImageView = itemView.findViewById<View>(R.id.prd_img) as ImageView
            prodName = itemView.findViewById<View>(R.id.prd_nme) as TextView
            prodType = itemView.findViewById<View>(R.id.prd_type) as TextView
            prodPrice = itemView.findViewById<View>(R.id.prd_prc) as TextView
            prodTax = itemView.findViewById<View>(R.id.prd_tax) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView: View = inflater.inflate(R.layout.item_swipe, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }
    override fun getItemCount(): Int {
        return swipeDataItemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Log.d(TAG, "holder.adapterPosition: ${holder.adapterPosition} position: $position")
        val item = swipeDataItemList[holder.adapterPosition]
        loadImage(holder, item.image)
        loadProdName(holder, item.product_name)
        loadProdType(holder, item.product_name)
        loadProdPrice(holder, item.price)
        loadProdTax(holder, item.tax)
    }

    private fun loadImage(holder: ViewHolder, url:String){
        Glide.with(holder.prodImageView.context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.prodImageView)
    }

    private fun loadProdName(holder: ViewHolder, prodName: String){
        val string = prodName.plus(" |")
        holder.prodName.text = string
        holder.prodName.setOnClickListener(clickListener)
    }

    private fun loadProdType(holder: ViewHolder, prodType: String){
        holder.prodType.text = prodType
        holder.prodType.setOnClickListener(clickListener)
    }


    private fun loadProdPrice(holder: ViewHolder, prodPrc: Double){
        val prdPriceBd = returnBigDecFormat(prodPrc)
        val rupeeString = holder.itemView.context.getString(R.string.rupee_sym)
        val string = rupeeString.plus(" $prdPriceBd | ")
        holder.prodPrice.text = string
        holder.prodPrice.setOnClickListener(clickListener)
    }

    private fun loadProdTax(holder: ViewHolder, prodTax: Double){
        val taxBigDec = returnBigDecFormat(prodTax)
        val rupeeString = holder.itemView.context.getString(R.string.rupee_sym)
        val string = rupeeString.plus(taxBigDec)
        holder.prodTax.text = string
        holder.prodTax.setOnClickListener(clickListener)
    }

    /**
     * given a double, this function return us a big decimal in the following format
     * if the given double is an integer, it returns a big decimal with no mantissa
     * if the given double is a non integer, it returns a big decimal with mantissa rounded to 3 digits.
     */
    private fun returnBigDecFormat(prodPrcTax:Double):String{
        //Log.d(TAG, "prodPrcTax: $prodPrcTax")
        val prdPriceBd = BigDecimal(prodPrcTax)
        val priceBigDec : BigDecimal = if(isIntegerValue(prdPriceBd)){
            BigDecimal(prodPrcTax)
        }else{
            BigDecimal(prodPrcTax).setScale(3,
                RoundingMode.HALF_EVEN)
        }
        return priceBigDec.toPlainString()
    }

    /**
     * function used to tell if a big decimal is an integer.
     */
    private fun isIntegerValue(bd: BigDecimal): Boolean {
        return (bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0)
    }
}
package com.onyx.android.sdk.data.request.cloud;


import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.model.Product;
import com.onyx.android.sdk.data.model.ProductQuery;
import com.onyx.android.sdk.data.model.ProductResult;
import com.onyx.android.sdk.data.utils.StoreUtils;

import com.onyx.android.sdk.data.v1.OnyxBookStoreService;
import com.onyx.android.sdk.data.v1.ServiceFactory;

import retrofit2.Response;

/**
 * Created by zhuzeng on 11/21/15.
 * Return the recommended products.
 */
public class ProductRecommendedListRequest extends BaseCloudRequest {

    private ProductResult<Product> productResult;
    private ProductQuery productQuery;

    public ProductRecommendedListRequest(ProductQuery productQuery) {
        this.productQuery = productQuery;
    }

    public final ProductResult<Product> getProductResult() {
        return productResult;
    }

    public void execute(final CloudManager parent) throws Exception {
        if (CloudManager.isWifiConnected(getContext())) {
            fetchFromCloud(parent);
        } else {
            fetchFromLocal(parent);
        }
    }

    public void fetchFromCloud(final CloudManager parent) throws Exception {
        OnyxBookStoreService service = ServiceFactory.getBookStoreService(parent.getCloudConf().getApiBase());
        Response<ProductResult<Product>> response = service.bookRecommendedList(JSON.toJSONString(productQuery)).execute();
        if (response.isSuccessful()) {
            productResult = response.body();
        }
    }

    public void fetchFromLocal(final CloudManager parent) throws Exception {
        productResult = new ProductResult<>();
        productResult.list = StoreUtils.queryDataList(Product.class);
        productResult.count = productResult.list.size();
    }

    private void saveToLocal(final ProductResult<Product> result) {
        StoreUtils.saveToLocal(result, Product.class, true);
    }
}

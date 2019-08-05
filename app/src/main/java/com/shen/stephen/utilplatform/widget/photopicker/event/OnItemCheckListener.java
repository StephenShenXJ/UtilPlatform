package com.shen.stephen.utilplatform.widget.photopicker.event;

import com.shen.stephen.utilplatform.widget.photopicker.entity.Photo;

/**
 * Created by donglua on 15/6/20.
 */
public interface OnItemCheckListener {

  /***
   *
   * @param position the position of the selected photo
   * @param photo     the selected photo
   *@param isCheck   specify whether the current photo is selected or not.
   * @param totalSelectedCount  the total selected count.
   * @return enable check
   */
  boolean OnItemCheck(int position, Photo photo, boolean isCheck, int totalSelectedCount);

}

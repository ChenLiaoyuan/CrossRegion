package com.haiyisoft.crossregion.test;

import org.junit.jupiter.api.Test;

/**
 * @author CLY
 * @date 2023/3/26 22:47
 **/
public class TestCopilot {

    @Test
    public void testCopilot(){
        System.out.println("test copilot");

        // 快速排序
        int[] arr = {1, 3, 2, 5, 4, 6, 7, 9, 8};
        quickSort(arr, 0, arr.length - 1);
        for (int i : arr) {
            System.out.print(i + " ");
        }

    }

    // 快速排序
    public static void quickSort(int[] arr, int left, int right) {
        if (left < right) {
            int i = left, j = right, x = arr[left];
            // 从左右两边交替扫描，直到i==j
            while (i < j) {
                // 从右向左找第一个小于x的数
                while (i < j && arr[j] >= x) {
                    j--;
                }
                // 找到了就把这个数放到左边
                if (i < j) {
                    arr[i++] = arr[j];
                }
                // 从左向右找第一个大于等于x的数
                while (i < j && arr[i] < x) {
                    i++;
                }
                // 找到了就把这个数放到右边
                if (i < j) {
                    arr[j--] = arr[i];
                }
            }
            // 把x放到中间
            arr[i] = x;
            // 递归调用
            quickSort(arr, left, i - 1);
            // 递归调用
            quickSort(arr, i + 1, right);
        }
    }

    // 二分查找
    public static int binarySearch(int[] arr, int key) {
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (key < arr[mid]) {
                high = mid - 1;
            } else if (key > arr[mid]) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    // 两数之和
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }

    // 冒泡排序
    public static void bubbleSort(int[] arr) {
        // 外层循环控制排序趟数（n个数排序，只需要排n-1次就行，最后一个数已经是有序了）
        for (int i = 0; i < arr.length - 1; i++) {
            // 内层循环控制每一趟排序多少次（后面的i个数已经是最小的了，无需参与排序，因为要和arr[j+1]比较，因此加到arr.length-1，避免数组越界）
            for (int j = 0; j < arr.length - 1 - i; j++) {
                // 相邻元素两两对比，元素交换位置
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }


}

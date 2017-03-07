from collections import deque


def merge_sort(lst):
    if len(lst) <= 1:
        return lst

    def merge(left, right):
        merged, left, right = deque(), deque(left), deque(right)
        while left and right:
            merged.append(left.popleft() if left[0] <= right[0] else right.popleft())
        merged.extend(right if right else left)
        return list(merged)

    mid = int(len(lst) // 2)
    left = merge_sort(lst[:mid])
    right = merge_sort(lst[mid:])
    return merge(left, right)


if __name__ == "__main__":
    print("Python")
    with open('not_sorted.txt') as inf:
        nums = inf.readlines()
    lst = [int(x.strip()) for x in nums]

    # Prompt user
    print("Sorting...")

    # Merge Sort
    sorted_lst = merge_sort(lst)

    with open('sorted_python.txt', 'w+') as outf:
        for x in sorted_lst:
            outf.write(str(x) + '\n')

    # Prompt user
    print("Done!")


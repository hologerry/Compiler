mergesort'merge :: (Ord a) => [a] -> [a] -> [a]
mergesort'merge [] xs = xs
mergesort'merge xs [] = xs
mergesort'merge (x:xs) (y:ys)
    | (x < y) = x:mergesort'merge xs (y:ys)
    | otherwise = y:mergesort'merge (x:xs) ys
 
mergesort'splitinhalf :: [a] -> ([a], [a])
mergesort'splitinhalf xs = (take n xs, drop n xs)
    where n = (length xs) `div` 2 
 
mergesort :: (Ord a) => [a] -> [a]
mergesort xs 
    | (length xs) > 1 = mergesort'merge (mergesort ls) (mergesort rs)
    | otherwise = xs
    where (ls, rs) = mergesort'splitinhalf xs

readLines :: FilePath -> IO [String]
readLines = fmap lines . readFile
makeInteger :: [String] -> [Int]
makeInteger = map read
makeString :: [Int] -> [String]
makeString = map show

main :: IO ()
main = do
    putStrLn "Haskell"
    contents <- readLines "not_sorted.txt"
    let lst = makeInteger contents
    putStrLn "Sorting..."
    let sorted_lst = mergesort lst

    let outFileName = "sorted_haskell.txt"
    writeFile outFileName $ unlines (makeString sorted_lst)
    putStrLn "Done!"
    

	.data
	.text
main:
	li	$t0, 10
	li	$t1, 100
	div	$t2, $t1, $t0
	bne	$t2, 10, L0
	mul	$t3, $t1, 2
	add	$t0, $t3, $t1
L0:
	addi	$t0, $t0, 2
L1:
	ble	$t0, $t1, L2
	addi	$t0, $t0, -10
	j	L1
L2:
	li	$t4, 100

